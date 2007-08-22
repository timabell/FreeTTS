This is the demo or a RTP player for FreeTTS.

1. Install JMF 2.1.1e
2. Create a file rtp.properties in the folder rtp with the following property
   jmf.dir=<YOUR PATH TO JMFHOM>
3. Start the JMStudio
4. Select 'File' and then 'Open RTP Session...' from the menu
5. Enter the following values
   address: 127.0.0.1
   port: 49150
   TTL: 1
6. Click Open
7. Run
   ant
   from the command line in the rtp folder