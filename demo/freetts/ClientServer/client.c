/*
 * Implements the Client side of the Client/Server demo.
 *
 * It waits for the user to type in a line of text, sends the line of
 * text to the speech server, which returns a stream of bytes (the
 * synthesized wave samples). This client then plays the stream
 * of bytes at the local audio device.
 *
 * You must start the speech server first. You can do this by typing:
 *
 * gmake runserver
 *
 * at the same directory. To run this client, modify set the speech
 * server host (and port number if not 5555) at the Makefile, and then type:
 * 
 * gmake runcclient
 *
 * In the Makefile, you can also specify the sample rate you want
 * as the third argument (currently, the server supports only 8kHz and 16kHz).
 *
 * This C client should run across most UNIX implementations, as it
 * uses standard UNIX system libraries.
 *
 * For a complete specification of the protocol between client and server,
 * consult the document <code>Protocol.txt</code>.
 */

#include <arpa/inet.h>
#include <ctype.h>
#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <sys/audio.h>
#include <sys/audioio.h>
#include <sys/filio.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>


#define TRUE 1
#define FALSE 0

#define SERVER_PORT 5555
#define SERVER_HOST "sunlabs.east"

#define AUDIO_DEVICE_FILE "/dev/audio"
#define AUDIO_DEVICE_ENV_VAR "AUDIODEV"  // for SunRays
#define DEFAULT_SAMPLE_RATE 8000

#define ADDRESS_SIZE sizeof(struct sockaddr_in)
#define AUDIO_BUFFER_SIZE 1024
#define TEXT_INPUT_BUFFER_SIZE 1024
#define STR_BUFFER_SIZE 10

#define FIRST_SENTENCE "Type in what you want me to say."


int connect_speech_server(char* server_host, int server_port);
void run_tts_protocol(int sock_fd);
int read_line(int sock_fd, char *buffer, int buffer_size);
int send_tts_request(int sock_fd, char *tts_text);
void receive_play_samples(int sock_fd, int number_samples);
int open_audio_device();
int set_pcm_linear();
unsigned char short_to_ulaw(short sample);
int is_string_nonempty(char *string, int length);


/* our audio device file descriptor */
int audio_fd;

/* the sample rate */
int sample_rate = DEFAULT_SAMPLE_RATE;

/* show metrics */
int metrics = 1;

/* equals 1 if the first byte is received */
int first_byte_received = 0;

/* the start time */
struct timeval start_time;

/* the first byte time */
struct timeval first_byte_time;

/* the first sound time */
struct timeval first_sound_time;




/**
 * It first attempts a connection to the speech server. Then,
 * it waits for the user to type in a line of text, sends the line of
 * text to the speech server, which returns a stream of bytes (the
 * synthesized wave samples). This client then plays the stream
 * of bytes at the local audio device.
 *
 * Arguments (optional):
 * argv[1] : the host name of speech server
 * argv[2] : the port number where the speech server is listening
 * argv[3] : the sample rate
 * argv[4] : show metrics, 1 to show, 0 to not show
 */
int main(int args, char *argv[]) {

  int sock_fd;
  int server_port;
  char* server_host;
  
  server_port = SERVER_PORT;
  server_host = SERVER_HOST;

  /* parse command line arguments for server hostname and port number */
  if (args >= 2) {
    server_host = argv[1];
  }
  if (args >= 3) {
    server_port = atoi(argv[2]);
  }
  if (args >= 4) {
    sample_rate = atoi(argv[3]);
  }
  if (args >= 5) {
    metrics = atoi(argv[4]);
  }

  /* connect to the server */
  sock_fd = connect_speech_server(server_host, server_port);

  /* start running the TTS protocol */
  run_tts_protocol(sock_fd);

  /* do cleanup */
  close(sock_fd);

  return 0;
}


/**
 * Connects to the remote speech server at the given host and port,
 * and returns the socket file descriptor to the connection.
 *
 * Arguments:
 * server_host: the host name of the speech server
 * server_port: the port on which the speech server is listening
 *
 * Returns:
 * a file descriptor of the socket
 */
int connect_speech_server(char* server_host, int server_port) {
  
  int sock_fd;
  struct sockaddr_in server = {AF_INET, SERVER_PORT};
  struct hostent *hp;

  /* obtain the IP address */

  hp = gethostbyname(server_host);
  if (hp == NULL) {
    perror("invalid hostname");
    exit(1);
  }

  /* set the IP address and port */

  bcopy((char *)hp->h_addr, (char *)&server.sin_addr, hp->h_length);
  server.sin_port = htons(server_port);

  
  /* set up the transport end point */

  if ((sock_fd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
    perror("socket call failed");
    exit(1);
  }

  /* connect to the server */

  if (connect(sock_fd, (struct sockaddr *) &server, ADDRESS_SIZE) == -1) {
    perror("connect call failed");
    exit(1);
  }

  return sock_fd;
}


/**
 * Runs the TTS protocol.
 * It waits for the user to type in a line of text, sends the line of
 * text to the speech server, which returns a stream of bytes (the
 * synthesized wave samples). This client then plays the stream
 * of bytes at the local audio device.
 *
 * Arguments:
 * sock_fd  the socket file descriptor
 */
void run_tts_protocol(int sock_fd) {

  char buffer[STR_BUFFER_SIZE];
  char input_buffer[TEXT_INPUT_BUFFER_SIZE];
  ssize_t nread;

  /* read the "READY" line from the Server */

  nread = recv(sock_fd, buffer, 6, 0);
  buffer[nread] = '\0';

  if (strcmp(buffer, "READY\n") == 0) {

    if (send_tts_request(sock_fd, FIRST_SENTENCE) == -1) {
      return;
    }
    
    input_buffer[0] = '\0';
    printf("Say       : ");
    while (fgets(input_buffer, TEXT_INPUT_BUFFER_SIZE, stdin) != NULL) {
      if (is_string_nonempty(input_buffer, strlen(input_buffer)) &&
	  send_tts_request(sock_fd, input_buffer) == -1) {
	return;
      }
      input_buffer[0] = '\0';
      printf("Say       : ");
    }
  }

  send(sock_fd, "DONE\n", 5, 0);

  /* drain all the audio before returning */

  ioctl(audio_fd, AUDIO_DRAIN, 0);

  printf("ALL DONE\n");
}


/**
 * Sends a TTS request of the given text to the given socket.
 *
 * Arguments:
 * sock_fd : socket file descriptor
 * tts_text : the text to perform TTS
 *
 * Returns:
 * 0 if everything's fine, -1 if any error occurred
 */
int send_tts_request(int sock_fd, char *tts_text) {

  int nsend;

  char tts_text_str[TEXT_INPUT_BUFFER_SIZE];
  int text_length;

  char number_samples_str[STR_BUFFER_SIZE];
  int number_samples;

  int input_length;
  input_length = strlen(tts_text);
  
  if (tts_text[input_length - 1] == '\n') {
    tts_text[input_length - 1] = '\0';
  }

  sprintf(tts_text_str, "TTS\n%d\n%s\n", sample_rate, tts_text);

  text_length = strlen(tts_text_str);

  /* record the time the request is sent */
  if (metrics) {
    gettimeofday(&start_time, NULL);
    first_byte_received = 0;
  }

  /*
   * send "TTS\n<sample_rate>\n<text>\n" (sent together to avoid 
   * repetitive send calls)
   */
  nsend = send(sock_fd, tts_text_str, text_length, 0);

  do {
    read_line(sock_fd, number_samples_str, STR_BUFFER_SIZE);
                                              /* how many samples? */

    if (strcmp(number_samples_str, "-2") == 0) {
      printf("TTS Error\n");
      return -1;
    }

    if (strcmp(number_samples_str, "-1") != 0) {
      number_samples = atoi(number_samples_str);
      
      printf("Receiving : %d samples\n", number_samples);

      receive_play_samples(sock_fd, number_samples);
    }
  }
  while (strcmp(number_samples_str, "-1") != 0 &&
	 strcmp(number_samples_str, "-2") != 0);

  if (metrics) {
    long elapsed_time =
      (first_byte_time.tv_sec - start_time.tv_sec)*1000 + 
      (first_byte_time.tv_usec - start_time.tv_usec)/1000;

    printf("FirstByte : %li ms\n", elapsed_time);
  }

  return 0;
}


/**
 * Receive the given number of wave samples and play it to the audio
 * device.
 *
 * Arguments:
 * sock_fd : the socket file descriptor
 * number_samples : the number of wave samples to receive from the socket
 */
void receive_play_samples(int sock_fd, int number_samples) {

  int nread;
  int nsend;
  int bytes_to_read;
  int bytes_remaining;
  short socket_buffer[AUDIO_BUFFER_SIZE];

  bytes_remaining = number_samples;

  open_audio_device();

  /* read the samples from the socket, and write it to the audio device */

  while (bytes_remaining > 0) {

    if (bytes_remaining >= AUDIO_BUFFER_SIZE) {
      bytes_to_read = AUDIO_BUFFER_SIZE;
    }
    else {
      bytes_to_read = bytes_remaining;
    }
    
    if ((nread = read(sock_fd, socket_buffer, bytes_to_read)) == -1) {
      perror("error reading samples");
    }

    if (metrics && !first_byte_received) {
      gettimeofday(&first_byte_time, NULL);
      first_byte_received = 1;
    }

    if ((nsend = write(audio_fd, socket_buffer, nread)) == -1) {
      perror("error playing samples");
    }

    bytes_remaining -= nread;
  }

  close(audio_fd);
}


/**
 * Reads a line of input from the given file descriptor, and save it
 * in the given buffer.
 *
 * Arguments:
 * sock_fd : the (socket) file descriptor
 * buffer : the buffer to save the line read
 * buffer_size : size of the buffer
 *
 * Returns:
 * The number of characters in the line, not including end of line character.
 */  
int read_line(int sock_fd, char *buffer, int buffer_size) {

  int i;
  char rc;

  for (i = 0; i < (buffer_size-1); i++) {
    read(sock_fd, &rc, 1);
    buffer[i] = rc;
    if (rc == '\n') {
      break;
    }
  }
  buffer[i] = '\0';
 
  return i;
}


/**
 * Returns 1 if the given string contains text, ie, it does not only
 * contain the space, newline or tab characters.
 *
 * Arguments:
 * string : the input string
 * length : the string length
 */
int is_string_nonempty(char *string, int length) {
  int i;
  for (i = 0; i < length; i++) {
    if (string[i] != ' ' && string[i] != '\n' && string[i] != '\t') {
      return 1;
    }
  }
  return 0;
}


/**
 * Opens the audio device file, and returns the file descriptor,
 * or -1 if an error occurred.
 *
 * Returns:
 * The audio device file descriptor.
 */
int open_audio_device() {
  
  char *audio_device = AUDIO_DEVICE_FILE;

  if ((audio_fd = open(audio_device, O_WRONLY)) == -1) {

    /* the device might be a SunRay, so get the $AUDIODEV env var */
    audio_device = getenv(AUDIO_DEVICE_ENV_VAR);
  
    if (audio_device != NULL) {
      if ((audio_fd = open(audio_device, O_RDWR)) == -1) {
	perror("Can't open audio device with environment variable");
	exit(1);
      }
    }
    else {
      perror("Can't open audio device");
      exit(1);
    }
  }

  if (set_pcm_linear() == FALSE) {
    perror("fail to set audio device to PCM linear");
    exit(1);
  }

  return audio_fd;
}


/**
 * Attempts to set the audio format of the audio device to 16-bit
 * PCM linear, at the given sample rate.
 *
 * Returns:
 * TRUE if the audio format was set successfully
 * FALSE otherwise
 */ 
int set_pcm_linear() {
  int set_status;

  audio_info_t info;
  // AUDIO_INITINFO(&info);

  ioctl(audio_fd, AUDIO_GETINFO, &info);

  info.play.encoding = AUDIO_ENCODING_LINEAR;
  info.play.precision = 16;
  info.play.channels = 1;
  info.play.sample_rate = sample_rate;

  set_status = ioctl(audio_fd, AUDIO_SETINFO, &info);

  if (set_status == -1) {
    return FALSE;
  } else {
    return TRUE;
  }
}
