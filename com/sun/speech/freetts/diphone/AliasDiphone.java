/*
 * Copyright (C) 2005 DFKI GmbH. All rights reserved.
 */
package com.sun.speech.freetts.diphone;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.speech.freetts.relp.Sample;

/**
 * Represent an alias diphone which is just another name for an
 * already-existing original diphone. This can be used just like a
 * "real" diphone. 
 * @author Marc Schr&ouml;der
 *
 */
public class AliasDiphone extends Diphone
{
    private String originalName;
    private Diphone original;
    /**
     * @param name The alias name to use for the existing diphone
     * @param originalName the original name of the diphone.
     */
    public AliasDiphone(String name, String originalName)
    {
        super(name);
        this.originalName = originalName;
    }

    /**
     * Get the name of the original name that this alias points to.
     */
    public String getOriginalName()
    {
        return originalName;
    }
    
    /**
     * Associate the actual diphone object of the original with this alias.
     * @param original a diphone object whose getName() must return the same
     * as our getOriginalName().
     * @throws IllegalArgumentException if the diphone to be registered as the
     * original has a name which is different from this AliasDiphone's original name
     * as returned by getOriginalName().
     */
    public void setOriginalDiphone(Diphone original)
    {
        if (!originalName.equals(original.getName())) {
            throw new IllegalArgumentException("The diphone to register ("+original.getName()
                    +") does not match the original name ("+originalName+")");
        }
        this.original = original;
    }
    
    /**
     * Returns the samples associated with this diphone.
     *
     * @return the samples associated with this diphone
     */
    public Sample[] getSamples() {
    return original.getSamples();
    }

    /**
     * Returns a particular sample.
     *
     * @param which which sample to return
     *
     * @return the desired sample
     */
    public Sample getSamples(int which) {
    return original.getSamples(which);
    }

    /**
     * Returns the midpoint index. the midpoint index is the sample
     * that divides the diphone into the first and second parts.
     *
     * @return the midpoint index.
     */
    public int getMidPoint() {
    return original.getMidPoint();
    }
    
    /**
     * Returns the midpoint index. the midpoint index is the sample
     * that divides the diphone into the first and second parts.
     *
     * @return the midpoint index.
     */
    public int getPbPositionMillis() {
    return original.getPbPositionMillis();
    }

    /**
     * Returns the sample that is closest to uIndex.
     *
     * @param uIndex the desired index
     * @param unitPart do we want the first have (1) or the second
     *      half (2)
     *
     * @return the sample nearest to the given index in the given
     *      part
     */ 
    public Sample nearestSample(float uIndex, int unitPart) {
        return original.nearestSample(uIndex, unitPart);
    }

    /**
     * Returns the total number of residuals in the given part for this
     * diphone.
     *
     * @param unitPart indicates which part is of interest (1 or 2)
     *
     * @return the number of residuals in the specified part
     */
    public int getUnitSize(int unitPart) {
        return original.getUnitSize(unitPart);
    }

    /**
     * dumps out this Diphone.
     */
    public void dump() {
    System.out.println("AliasDiphone: " + getName() + " aliased to " + original.getName());
    }

    /**
     * Dumps the diphone to the given channel.
     *
     * @param bb the ByteBuffer to write to
     *
     * @throws IOException if IO error occurs
     */
    public void dumpBinary(ByteBuffer bb) throws IOException {
        char[] nameArray = (getName() + "        ").toCharArray();
        char[] origNameArray = (original.getName() + "        ").toCharArray();

        bb.putInt(ALIAS_MAGIC);
        for (int i = 0; i < NAME_LENGTH; i++) {
            bb.putChar(nameArray[i]);
        }
        for (int i = 0; i < NAME_LENGTH; i++) {
            bb.putChar(origNameArray[i]);
        }
    }

    /**
     * Dumps the diphone to the given channel.
     *
     * @param os the DataOutputStream to write to
     *
     * @throws IOException if IO error occurs
     */
    public void dumpBinary(DataOutputStream os) throws IOException {
        char[] nameArray = (getName() + "        ").toCharArray();
        char[] origNameArray = (original.getName() + "        ").toCharArray();

        os.writeInt(ALIAS_MAGIC);
        for (int i = 0; i < NAME_LENGTH; i++) {
            os.writeChar(nameArray[i]);
        }
        for (int i = 0; i < NAME_LENGTH; i++) {
            os.writeChar(origNameArray[i]);
        }
    }

    /**
     * Determines if the two diphones are equivalent. This is for
     * testing databases. This is not the same as "equals".
     * For aliased diphones, this will return true if the compare() method
     * of the original diphone returns true, even if the name used in this alias 
     * is different.
     *
     * @param other the diphone to compare this one to
     *
     * @return <code>true</code> if the diphones match; otherwise
     *      <code>false</code> 
     */
    boolean compare(Diphone other) {
        return original.compare(other);
    }

}
