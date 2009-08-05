/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.engine.synthesis.text;

import java.util.Enumeration;
import java.util.Vector;

import javax.speech.Engine;
import javax.speech.EngineStateError;
import javax.speech.synthesis.SynthesizerModeDesc;

import com.sun.speech.engine.synthesis.BaseSynthesizer;
import com.sun.speech.engine.synthesis.BaseSynthesizerQueueItem;

/**
 * Supports a simple text-output-only JSAPI 1.0 <code>Synthesizer</code>.
 * Intended for demonstration purposes for those developing JSAPI
 * implementations.  It may also be useful to developers who want a
 * JSAPI synthesizer that doesn't produce any noise.
 */
public class TextSynthesizer extends BaseSynthesizer {
    /**
     * Reference to output thread.
     */
    OutputHandler outputHandler = null;

    /**
     * Creates a new Synthesizer in the DEALLOCATED state.
     *
     * @param desc the operating mode
     */
    public TextSynthesizer(SynthesizerModeDesc desc) {
        super(desc);
        outputHandler = new OutputHandler();
    }

    /**
     * Starts the output thread.
     */
    protected void handleAllocate() {
        long states[];
        synchronized (engineStateLock) {
            long newState = ALLOCATED | RESUMED;
            newState |= (outputHandler.isQueueEmpty()
                         ? QUEUE_EMPTY
                         : QUEUE_NOT_EMPTY);
            states = setEngineState(CLEAR_ALL_STATE, newState);
        }
        outputHandler.start();
        postEngineAllocated(states[0], states[1]);
    }

    /**
     * Stops the output thread.
     */
    protected void handleDeallocate() {
        long[] states = setEngineState(CLEAR_ALL_STATE, DEALLOCATED);
        cancelAll();
        outputHandler.terminate();
        postEngineDeallocated(states[0], states[1]);
    }
    
    /**
     * Creates a TextSynthesizerQueueItem.
     *
     * @return a TextSynthesizerQueueItem
     */
    protected BaseSynthesizerQueueItem createQueueItem() {
        return new TextSynthesizerQueueItem();
    }

    /**
     * Returns an enumeration of the queue.
     *
     * @return
     *   an <code>Enumeration</code> of the speech output queue or
     *   <code>null</code>.
     *
     * @throws EngineStateError 
     *   if this <code>Synthesizer</code> in the <code>DEALLOCATED</code> or 
     *   <code>DEALLOCATING_RESOURCES</code> states
     */
    public Enumeration enumerateQueue() throws EngineStateError {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        return outputHandler.enumerateQueue();
    }

    /**
     * Puts an item on the speaking queue and sends a queue updated
     * event.  Expects only <code>TextSynthesizerQueueItems</code>.
     *
     * @param item the item to add to the queue
     *
     */
    protected void appendQueue(BaseSynthesizerQueueItem item) {
        outputHandler.appendQueue((TextSynthesizerQueueItem) item);
    }

    /**
     * Cancels the item at the top of the queue.
     *
     * @throws EngineStateError 
     *   if this <code>Synthesizer</code> in the <code>DEALLOCATED</code> or 
     *   <code>DEALLOCATING_RESOURCES</code> states
     */
    public void cancel() throws EngineStateError {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelItem();
    }

    /**
     * Cancels a specific object on the queue.
     *
     * @param source
     *    object to be removed from the speech output queue
     *
     * @throws IllegalArgumentException
     *  if the source object is not found in the speech output queue.
     * @throws EngineStateError 
     *   if this <code>Synthesizer</code> in the <code>DEALLOCATED</code> or 
     *   <code>DEALLOCATING_RESOURCES</code> states
     */
    public void cancel(Object source)
        throws IllegalArgumentException, EngineStateError {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelItem(source);
    }

    /**
     * Cancels all items on the output queue.
     *
     * @throws EngineStateError 
     *   if this <code>Synthesizer</code> in the <code>DEALLOCATED</code> or 
     *   <code>DEALLOCATING_RESOURCES</code> states
     */
    public void cancelAll() throws EngineStateError {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelAllItems();
    }

    /**
     * Pauses the output.
     */
    protected void handlePause() {
        outputHandler.pauseItem();
    }    

    /**
     * Resumes the output.
     */
    protected void handleResume() {
        outputHandler.resumeItem();
    }

    /**
     * The output device for a <code>TextSynthesizer</code>.  Sends
     * all text to standard out.
     */
    public class OutputHandler extends Thread {
        protected boolean done = false;
        
        /**
         * Internal speech output queue that will contain a set of 
         * TextSynthesizerQueueItems.
         *
         * @see BaseSynthesizerQueueItem
         */
        protected Vector queue;

        /**
         * The current item to speak.
         */
        TextSynthesizerQueueItem currentItem;
    
        /**
         * Object to lock on for setting the current item.
         */
        protected Object currentItemLock = new Object();
        
        /**
         * Current output "speaking" rate.
         * Updated as /rate[166.3]/ controls are detected in the output text.
         */
        int rate = 100;

        /**
         * For the item at the top of the queue, the output command reflects 
         * whether item should be PAUSE, RESUME, CANCEL.
         */
        protected int command;

        protected final static int PAUSE = 0;
        protected final static int RESUME = 1;
        protected final static int CANCEL = 2;
        protected final static int CANCEL_ALL = 3;
        protected final static int CANCEL_COMPLETE = 4;

        /**
         * Object on which accesses to the command must synchronize.
         */
        protected Object commandLock = new Object();

        /**
         * Class constructor.
         */
        public OutputHandler() {
            queue = new Vector();
            currentItem = null;
        }

        /**
         * Stops execution of the Thread.
         */
        public void terminate() {
            done = true;
        }
        
        /**
         * Returns the current queue.
         *
         * @return the current queue
         */
        public Enumeration enumerateQueue() {
            synchronized(queue) {
                return queue.elements();
            }
        }

        /**
         * Determines if the queue is empty.
         *
         * @return <code>true</code> if the queue is empty
         */
        public boolean isQueueEmpty() {
            synchronized(queue) {
                return queue.size() == 0;
            }
        }
        
        /**
         * Adds an item to be spoken to the output queue.
         *
         * @param item the item to be added
         */
        public void appendQueue(TextSynthesizerQueueItem item) {
            boolean topOfQueueChanged;
            synchronized(queue) {
                topOfQueueChanged = (queue.size() == 0);
                queue.addElement(item);
                queue.notifyAll();
            }            
            if (topOfQueueChanged) {
                long[] states = setEngineState(QUEUE_EMPTY,
                                               QUEUE_NOT_EMPTY);
                postQueueUpdated(topOfQueueChanged, states[0], states[1]);
            }
        }

        /**
         * Cancels the current item.
         */
        protected void cancelItem() {
            cancelItem(CANCEL);
        }
        
        /**
         * Cancels all items.
         */
        protected void cancelAllItems() {
            cancelItem(CANCEL_ALL);
        }
        
        /**
         * Cancels all or just the current item.
         *
         * @param cancelType <code>CANCEL</code> or <code>CANCEL_ALL</code>
         */
        protected void cancelItem(int cancelType) {
            synchronized(queue) {
                if (queue.size() == 0) {
                    return;
                }
            }
            synchronized(commandLock) {
                command = cancelType;
                commandLock.notifyAll();
                while (command != CANCEL_COMPLETE) {
                    try {
                        commandLock.wait();
                    } catch (InterruptedException e) {
                        // Ignore interrupts and we'll loop around
                    }
                }
                if (testEngineState(Engine.PAUSED)) {
                    command = PAUSE;
                } else {
                    command = RESUME;
                }
                commandLock.notifyAll();
            }
        }
            
        /**
         * Cancels the given item.
         *
         * @param source the item to cancel
         */
        protected void cancelItem(Object source) {
//              synchronized(currentItemLock) {
//                  if (currentItem.getSource() == source) {
//                      cancelItem();
//                  } else {
//                      boolean queueEmptied;
//                      synchronized(queue) {
//                          for (int i = 0; i < queue.size(); i++) {
//                              BaseSynthesizerQueueItem item =
//                                  (BaseSynthesizerQueueItem)(queue.elementAt(i));
//                              if (item.getSource() == source) {
//                                  item.postSpeakableCancelled();
//                                  queue.removeElementAt(i);
//                              }
//                          }
//                          queueEmptied = queue.size() == 0;
//                          queue.notifyAll();
//                      }
//                      if (queueEmptied) {
//                          long[] states = setEngineState(QUEUE_NOT_EMPTY,
//                                                         QUEUE_EMPTY);
//                          postQueueEmptied(states[0], states[1]);
//                      } else { 
//                          long[] states = setEngineState(QUEUE_NOT_EMPTY,
//                                                         QUEUE_NOT_EMPTY);
//                          postQueueUpdated(false, states[0], states[1]);
//                      }
//                  }
//              }
        }

        /**
         * Pauses the output.
         */
        protected void pauseItem() {
            synchronized(commandLock) {
                if (command != PAUSE) {
                    command = PAUSE;
                    commandLock.notifyAll();
                }
            }
        }

        /**
         * Resumes the output.
         */
        protected void resumeItem() {
            synchronized(commandLock) {
                if (command != RESUME) {
                    command = RESUME;
                    commandLock.notifyAll();
                }
            }
        }

        /**
         * Controls output of text until terminate is called.
         *
         * @see #terminate
         */
        public void run() {
            TextSynthesizerQueueItem item;
            int currentCommand;
            boolean queueEmptied;
            
            if (testEngineState(Engine.PAUSED)) {
                command = PAUSE;
            } else {
                command = RESUME;
            }
            
            while (!done) {
                item = getQueueItem();
                item.postTopOfQueue();
                currentCommand = outputItem(item);
                if (currentCommand == CANCEL_ALL) {
                    Vector itemList = new Vector();
                    itemList.add(item);
                    synchronized(queue) {
                        queue.remove(0);
                        while (queue.size() > 0) {
                            itemList.add(queue.remove(0));
                        }
                    }
                    synchronized(commandLock) {
                        command = CANCEL_COMPLETE;
                        commandLock.notifyAll();
                    }
                    while (itemList.size() > 0) {
                        item = (TextSynthesizerQueueItem)(itemList.remove(0));
                        item.postSpeakableCancelled();
                    }
                    long[] states = setEngineState(QUEUE_NOT_EMPTY,
                                                   QUEUE_EMPTY);
                    postQueueEmptied(states[0], states[1]);
                    continue;
                } else if (currentCommand == CANCEL) {
                    synchronized(commandLock) {
                        command = CANCEL_COMPLETE;
                        commandLock.notifyAll();
                    }
                    item.postSpeakableCancelled();
                } else if ((currentCommand == PAUSE)
                    || (currentCommand == RESUME)) {
                    item.postSpeakableEnded();
                }
                
                synchronized(queue) {
                    queue.remove(0);
                    queueEmptied = queue.size() == 0;
                    queue.notifyAll();
                }                

                if (queueEmptied) {
                    long[] states = setEngineState(QUEUE_NOT_EMPTY,
                                                   QUEUE_EMPTY);
                    postQueueEmptied(states[0], states[1]);
                } else { 
                    long[] states = setEngineState(QUEUE_NOT_EMPTY,
                                                   QUEUE_NOT_EMPTY);
                    postQueueUpdated(true, states[0], states[1]);
                }
            }
        }

        /**
         * Returns, but does not remove, the first item on the queue.
         *
         * @return the first item on the queue
         */
        protected TextSynthesizerQueueItem getQueueItem() {
            synchronized(queue) {
                while (queue.size() == 0) {
                    try {
                        queue.wait();
                    }
                    catch (InterruptedException e) {
                        // Ignore interrupts and we'll loop around
                    }
                }
                return (TextSynthesizerQueueItem) queue.elementAt(0);
            }
        }

        /**
         * Starts outputting the item.  Returns the current command.
         *
         * @param item to be output
         *
         * @return the current command
         */
        protected int outputItem(TextSynthesizerQueueItem item) {
            int currentCommand;
            String engineText;
            int engineTextIndex;
            boolean wasPaused = false;
            
            System.out.println("----- BEGIN: "
                               + item.getTypeString()
                               + "-----");

            engineText = item.getEngineText();
            engineTextIndex = 0;

            // [[[WDW - known danger with this loop -- the actual
            // command could change between the times it is checked.
            // For example, a call to pause followed by resume
            // followed by a pause might go unnoticed.]]]
            //
            synchronized(commandLock) {
                currentCommand = command;
            }
            while (engineTextIndex < engineText.length()) {
                // On a pause, just hang out and wait.  If the text
                // index is not 0, it means we've already started some
                // processing on the current item.
                //
                if (currentCommand == PAUSE) {
                    if (engineTextIndex > 0) {
                        item.postSpeakablePaused();
                        wasPaused = true;
                    }
                    synchronized(commandLock) {
                        while (command == PAUSE) {
                            try {
                                commandLock.wait();
                            } catch (InterruptedException e) {
                                // Ignore interrupts and we'll loop around
                            }
                        }
                        currentCommand = command;
                    }
                }

                // On a resume, send out some text.  If the text index
                // is 0, it means we are just starting processing of
                // this speakable and we need to post an event saying
                // so.
                //
                if (currentCommand == RESUME) {
                    if (engineTextIndex == 0) {
                        item.postSpeakableStarted();
                    } else if (wasPaused) {
                        item.postSpeakableResumed();
                        wasPaused = false;
                    }

                    // If we get here, then we're processing text
                    // Consider three options
                    // 1. Next char is the start of a synth directive
                    //    such as /emp[1]/
                    // 2. Next char is the start of white space
                    // 3. Next char is the start of plain text
                    //
                    if (isCommand(engineText, engineTextIndex)) {
                        engineTextIndex = processCommand(item,
                                                         engineText,
                                                         engineTextIndex);
                    } else if (isWhitespace(engineText, engineTextIndex)) {
                        engineTextIndex = processWhitespace(engineText,
                                                            engineTextIndex);
                    } else {
                        engineTextIndex = processNormalText(item,
                                                            engineText,
                                                            engineTextIndex);
                    }
                } else {
                    // Otherwise, the command is CANCEL or CANCEL_ALL
                    // and we should get out of this loop.
                    //
                    break;
                }
                synchronized(commandLock) {
                    currentCommand = command;
                }
            }
            
            System.out.println("\n----- END: "
                               + item.getTypeString()
                               + "-----\n");
            
            return currentCommand;
        }        
            
        /**
         * Determines if the next thing in line is a command.
         *
         * @param engineText the text containing embedded commands
         * @param index the current index
         *
         * @return <code>true</code> if the next thing in line is a command
         */
        protected boolean isCommand(String engineText, int index) {
            if (!engineText.substring(index,index + 1).equals(
                TextSynthesizerQueueItem.COMMAND_PREFIX)) {
                return false;
            }
            
            // Test for all known commands
            //
            for (int i = 0;
                 i < TextSynthesizerQueueItem.ELEMENTS.length;
                 i++) {
                if (engineText.startsWith(
                        TextSynthesizerQueueItem.COMMAND_PREFIX
                        + TextSynthesizerQueueItem.ELEMENTS[i], index)) {
                    return true;
                }
            }
            return false;
        }
    
        /**
         * Attempts to process a command starting at the next character
         * in the synthesizer text. Returns the new index.
         *
         * @param item the current queue item
         * @param engineText the text containing embedded commands
         * @param index the current index
         *
         * @return the new index
         */
        protected int processCommand(TextSynthesizerQueueItem item,
                                     String engineText, int index) {
            // Test for all known commands
            //
            for (int i = 0;
                 i < TextSynthesizerQueueItem.ELEMENTS.length;
                 i++) {
                if (engineText.startsWith(
                        TextSynthesizerQueueItem.COMMAND_PREFIX
                        + TextSynthesizerQueueItem.ELEMENTS[i], index)) {
                    int endIndex = engineText.indexOf(
                        TextSynthesizerQueueItem.COMMAND_SUFFIX, index+1)
                        + 1;
                    String commandText = engineText.substring(index, endIndex);
                    System.out.print(commandText);
                    System.out.flush();
                    return endIndex;
                }
            }
            return index;
        }


        /**
         * Determines if there is whitespace at the current index.
         *
         * @param engineText the text containing embedded commands
         * @param index the current index
         *
         * @return <code>true</code> if there is whitespace at the
         *   current index
         */
        protected boolean isWhitespace(String engineText, int index) {
            return Character.isWhitespace(engineText.charAt(index));
        }

        /**
         * Processes whitespace at the current index in the synthesizer text.
         * If next character is not whitespace, does nothing.
         * If next character is whitespace, displays it and pauses
         * briefly to simulate the speaking rate.
         *
         * @param engineText the text containing embedded commands
         * @param index the current index
         *
         * @return the new index
         */
        protected int processWhitespace(String engineText, int index) {
            // Identify full span of whitespace
            //
            int endIndex = index;
            while (endIndex < engineText.length() && 
                   Character.isWhitespace(engineText.charAt(endIndex))) {
                endIndex++;
            }

            // Display the whitespace as plain text
            //
            System.out.print(engineText.substring(index, endIndex));
            System.out.flush();

            // Pause briefly with the delay determined by the current
            // "speaking rate."  Convert the word-per-minute rate to
            // millseconds.
            //
            try {
                sleep(1000 * 60 / rate);
            } catch (InterruptedException e) {
                // Ignore any interruption
            }

            return endIndex;
        }

        /**
         * Processes next set of characters in output up to whitespace
         * or next '/' that could indicate the start of a command.
         *
         * @param item the current queue item
         * @param engineText the text containing embedded commands
         * @param index the current index
         *
         * @return the new index
         */
        protected int processNormalText(TextSynthesizerQueueItem item,
                                        String engineText,
                                        int index) {
            String wordStr;
            
            // Find the end of the plain text
            //
            int endIndex = index+1;
            while (endIndex < engineText.length() && 
                   engineText.charAt(endIndex) != '/' &&
                   !Character.isWhitespace(engineText.charAt(endIndex)))
                endIndex++;

            // Display the text in a plain format
            //
            wordStr = engineText.substring(index, endIndex);
            item.postWordStarted(wordStr, index, endIndex);
            System.out.print(wordStr);
            System.out.flush();
            return endIndex;
        }
    }
}
