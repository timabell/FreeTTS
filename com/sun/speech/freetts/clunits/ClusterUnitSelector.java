/**
 * Portions Copyright 2001-2003 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.clunits;


import java.util.List;
import java.util.Iterator;
import java.net.URL;
import java.io.IOException;

import com.sun.speech.freetts.relp.Sample;
import com.sun.speech.freetts.relp.SampleInfo;
import com.sun.speech.freetts.relp.SampleSet;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.cart.CART;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.FeatureSet;
import com.sun.speech.freetts.FeatureSetImpl;
import com.sun.speech.freetts.PathExtractor;
import com.sun.speech.freetts.PathExtractorImpl;

import de.dfki.lt.freetts.ClusterUnitNamer;


/**
 * Generates the Unit Relation of an Utterance from the
 * Segment Relation.
 *
 */
public class ClusterUnitSelector implements UtteranceProcessor {

    final static boolean DEBUG = false;
    private final static PathExtractor DNAME = new PathExtractorImpl(
	    "R:SylStructure.parent.parent.name", true);
    private ClusterUnitDatabase clunitDB;
    private ClusterUnitNamer unitNamer;
    
    /**
     * Constructs a ClusterUnitSelector.
     *
     * @param url the URL for the unit database. If the URL path ends
     *     with a '.bin' it is assumed that the DB is a binary database,
     *     otherwise, its assumed that its a text database1
     *
     * @throws IOException if an error occurs while loading the
     *     database
     *
     */
    public ClusterUnitSelector(URL url) throws IOException {
        this(url, null);
    }
    
    /**
     * Constructs a ClusterUnitSelector.
     *
     * @param url the URL for the unit database. If the URL path ends
     *     with a '.bin' it is assumed that the DB is a binary database,
     *     otherwise, its assumed that its a text database1
     * @param unitNamer an optional unit namer, specifying how the cluster
     * units are called in the voice database referenced by url. If this is null,
     * an ldom unit naming scheme will be used (e.g., 'ae_afternoon' for the
     * phoneme 'ae' in the word 'afternoon'.
     *
     * @throws IOException if an error occurs while loading the
     *     database
     *
     */
    public ClusterUnitSelector(URL url, ClusterUnitNamer unitNamer) throws IOException {
        if (url == null) {
	    throw new IOException("Can't load cluster unit database");
	}
	boolean binary = url.getPath().endsWith(".bin");
	clunitDB = new ClusterUnitDatabase(url, binary);
	this.unitNamer = unitNamer; 
    }
    
    /**
     * Generates the Unit Relation from the Segment Relation.
     * <br><b>Implementation note:</b><br>
     *    Populates the segment relation with segment names of the form:
     *    XX_YY where XX is the segment name (typically a phoneme)
     *    and YY is the word that the segment is in (stripped and
     *    lower case).
     *
     *    The first step in cluster unit selection is to determine the unit
     * 	  type for each unit in the utterance. The unit type for
     * 	  selection in the simple talking clock example (cmu_time_awb) is
     * 	  done per phone. The unit type consists of the phone
     * 	  name followed by the word the phone comes from (e.g., n_now for
     * 	  the phone 'n' in the word 'now'). 
     *
     *   Invoke the Viterbi algorithm (via a viterbi class) that 
     *   selects the proper units for the segment and adds that to
     *   each segment item.
     *
     *   For each segment, create a unit and attach features based
     *   upon the selected units.
     *
     * @param  utterance  the utterance to generate the Unit Relation
     *
     * @throws ProcessException if an IOException is thrown during the
     *         processing of the utterance
     * 
     */
    public void processUtterance(Utterance utterance) throws ProcessException {
	Viterbi vd;
	Relation segs = utterance.getRelation(Relation.SEGMENT);

	utterance.setObject(SampleInfo.UTT_NAME,
		clunitDB.getSampleInfo());
    	utterance.setObject("sts_list", clunitDB.getSts());

	vd = new Viterbi(segs, clunitDB);

	for (Item s = segs.getHead(); s != null; s = s.getNext()) {
	    setUnitName(s);
	}

	vd.decode();

	if (!vd.result("selected_unit")) {
	    utterance.getVoice().error("clunits: can't find path");
	}

	vd.copyFeature("unit_prev_move");
	vd.copyFeature("unit_this_move");

	Relation unitRelation = utterance.createRelation(Relation.UNIT);

	for (Item s = segs.getHead(); s != null; s = s.getNext()) {
	    Item unit = unitRelation.appendItem();
	    FeatureSet unitFeatureSet = unit.getFeatures();
	    int unitEntry = s.getFeatures().getInt("selected_unit");

	    unitFeatureSet.setString("name", s.getFeatures().getString("name"));

	    int unitStart;
	    int unitEnd;
	    String clunitName = s.getFeatures().getString("clunit_name");

	    if (s.getFeatures().isPresent("unit_this_move")) {
		unitStart = s.getFeatures().getInt("unit_this_move");
	    } else {
		unitStart = clunitDB.getStart(unitEntry);
	    }

	    if (s.getNext() != null &&
		    s.getNext().getFeatures().isPresent("unit_prev_move")) {
		unitEnd = s.getNext().getFeatures().getInt("unit_prev_move");
	    } else {
		unitEnd = clunitDB.getEnd(unitEntry);
	    }

	    unitFeatureSet.setInt("unit_entry", unitEntry);
	    ClusterUnit clunit = new ClusterUnit(clunitDB, 
		    clunitName, unitStart, unitEnd);
	    unitFeatureSet.setObject("unit", clunit);
	    if (true) { 
		unitFeatureSet.setInt("unit_start", clunit.getStart());
		unitFeatureSet.setInt("unit_end", clunit.getEnd());
	    } // add the rest of these things for debugging.

	    if (DEBUG) {
		debug(" sr " + clunitDB.getSampleInfo().getSampleRate() + " " +
		    s.getFeatures().getFloat("end") + " " +
		    (int) (s.getFeatures().getFloat("end") * 
			   clunitDB.getSampleInfo().getSampleRate()));
	    }
	    unitFeatureSet.setInt("target_end", 
		(int) (s.getFeatures().getFloat("end") 
		       * clunitDB.getSampleInfo().getSampleRate()));
	}
    }


    /**
     * Sets the cluster unit name given the segment.
     *
     * @param seg the segment item that gets the name
     */
    protected void setUnitName(Item seg) {
        if (unitNamer != null) {
            unitNamer.setUnitName(seg);
            return;
        }
        // default to LDOM naming scheme 'ae_afternoon':
	String cname = null;

	String segName = seg.getFeatures().getString("name");

	if (segName.equals("pau")) {
	    cname = "pau_" + seg.findFeature("p.name");
	} else {
	    // remove single quotes from name
	    String dname = ((String) DNAME.findFeature(seg)).toLowerCase();
	    cname = segName + "_" + stripQuotes(dname);
	}
	seg.getFeatures().setString("clunit_name", cname);
    }


    /**
     * Strips quotes from the given string.
     *
     * @param s the string to strip quotes from
     *
     * @return a string with all single quotes removed
     */
    private String stripQuotes(String s) {
	StringBuffer sb = new StringBuffer(s.length());
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if (c != '\'') {
		sb.append(c);
	    }
	}
	return sb.toString();
    }


    /**
     * Retrieves the string representation of this object.
     * 
     * @return the string representation of this object
     */
    public String toString() {
	return "ClusterUnitSelector";
    }

    /**
     * Provides support for the Viterbi Algorithm.
     *
     * Implementation Notes
     * <p>
     * For each candidate for the current unit, calculate the cost
     * between it and the first candidate in the next unit.  Save
     * only the path that has the least cost. By default, if two
     * candidates come from units that are adjacent in the
     * database, the cost is 0 (i.e., they were spoken together,
     * so they are a perfect match).
     * <p>
     * 
     * Repeat the previous process for each candidate in the next
     * unit, creating a list of least cost paths between the
     * candidates between the current unit and the unit following
     * it.
     * <p>
     * 
     * Toss out all candidates in the current unit that are not
     * included in a path.
     * <p>
     * 
     * Move to the next unit and repeat the process.
    */
    static class Viterbi {
	private int numStates = -1;
	private boolean bigIsGood = false;
	private ViterbiPoint timeline = null;
	private ViterbiPoint lastPoint = null;
	private FeatureSet f = null;
	private ClusterUnitDatabase clunitDB;

	/**
	 * Creates a Viterbi class to process the given segment.
	 */
	public Viterbi(Relation segs, ClusterUnitDatabase db) {
	    ViterbiPoint last = null;
	    clunitDB = db;
	    f = new FeatureSetImpl();
	    for (Item s = segs.getHead(); true; s = s.getNext()) {
		ViterbiPoint n = new ViterbiPoint(s);
		if (numStates > 0) {
		    n.initPathArray(numStates);
		}
		if (last != null) { 
		    last.next = n;
		} else {
		    timeline = n;
		}
		last = n;

		if (s == null) {
		    lastPoint = n;
		    break;
		}
	    }

	    if (DEBUG) {
		debug("num states " + numStates);
	    }

	    if (numStates == 0) {    	// its a  general beam search
		timeline.paths = new ViterbiPath();
	    }

	    if (numStates == -1) {	// dynamic number of states (# cands)
		timeline.initPathArray(1);
	    }
	}

	/**
	 * Sets the given feature to the given value.
	 *
	 * @param name the name of the feature
	 * @param obj the new value.
	 */
	public void setFeature(String name, Object obj) {
	    f.setObject(name, obj);
	}

	/**
	 * Gets the value for the given feature.
	 *
	 * @param name the name of the feature
	 *
	 * @return the value of the feature
	 */
	public Object getFeature(String name) {
	    return f.getObject(name);
	}

	/**
	 * Find the best path candidate.
	 */
	void decode() {
	    for (ViterbiPoint p = timeline; p.next != null; p = p.next) {
		p.cands = getCandidate(p.item);
		if (DEBUG) {
		    debug("decode " + p.cands);
		}
		if (numStates != 0) {
		    if (numStates == -1) {
			p.next.initDynamicPathArray(p.cands);
		    }

		    for (int i = 0; i < p.numStates; i++) {
			if ((p == timeline && i == 0) || 
				(p.statePaths[i] != null)) {
			    // debug("   dc p " + p);
			    for (ViterbiCandidate c = p.cands; 
					c != null; c = c.next) {
				ViterbiPath np = getPath(p.statePaths[i], c);
				addPaths(p.next, np);
			    }
			}
		    }
		} else {
		    System.err.println(
			"Viterbi.decode: general beam search not implemented");
		}
	    }
	}


	/**
	 * Try to add paths to the given point.
	 *
	 * @param point the point to add the paths to
	 * @param paths the path
	 */
	void addPaths(ViterbiPoint point, ViterbiPath path) {
	    ViterbiPath nextPath;
	    for (ViterbiPath p = path; p != null; p = nextPath) {
		nextPath = p.next;
		addPath(point, p);
	    }
	}

	/**
	 * Add the new path to the state path if it is
	 * better than the current path.
	 *
	 * @param point where the path is added
	 * @param newPath the path to try to add.
	 */
	void addPath(ViterbiPoint point, ViterbiPath newPath) {
	    if (point.statePaths[newPath.state] == null) {
		// we don't have one yet, so this is best
		point.statePaths[newPath.state] = newPath;
	    } else if (isBetterThan(newPath.score,
			point.statePaths[newPath.state].score)) {
		// its better than what we already have 
		point.statePaths[newPath.state] = newPath;
	    } else {
		// its not better that what we already have
		// so we just forget about it.
	    }
    	}

	/**
	 * See if a is better than b. Goodness is defined
	 * by 'bigIsGood'.
	 *
	 * @param a value to check
	 * @param b value to check.
	 *
	 * return true if a is better than b.
	 */
	private boolean isBetterThan(int a, int b) {
	    if (bigIsGood) {
		return a > b;
	    } else {
		return a < b;
	    }
	}

	/**
	 * Find the best path through the decoder, adding the feature
	 * name to the candidate.
	 *
	 * @param feature the feature to add
	 * @return true if a best path was found
	 */
	boolean  result(String feature) {
	    ViterbiPath path;

	    if (timeline == null || timeline.next == null) {
		return true; // null case succeeds
	    }
	    path = findBestPath();

	    if (path == null) {
		return false;
	    }

	    for (; path != null; path = path.from) {
		if (path.candidate != null) {
		    path.candidate.item.getFeatures().setObject(feature,
			    	path.candidate.value);
		}
	    }
	    return true;
	}

	/**
	 * Given a feature, copy the value associated with feature
	 * name from the path to each item in the path.
	 *
	 * @param feature the name of the feature.
	 */
	void copyFeature(String feature) {
	    ViterbiPath path = findBestPath();
	    if (path == null) { 
		return;  // nothing to copy, empty stream or no solution
	    }

	    for (; path != null; path = path.from) {
		if (path.candidate != null && path.isPresent(feature)) {
		    path.candidate.item.getFeatures().setObject(feature,
			    path.getFeature(feature));
		}
	    }
	}

	/**
	 * Finds the best candiate.
	 */
	private ViterbiCandidate getCandidate(Item item) {
	    String unitType = item.getFeatures().getString("clunit_name");
	    CART cart = clunitDB.getTree(unitType);
	    int[] clist = (int[]) cart.interpret(item);
	    ViterbiCandidate p;
	    ViterbiCandidate all;
	    ViterbiCandidate gt;

	    all = null;
	    for (int i = 0; i < clist.length; i++) {
		p = new ViterbiCandidate();
		p.next = all;
		p.item = item;
		p.score = 0;
		p.setInt(clunitDB.getUnitIndex(unitType,  clist[i]));
		all = p;
		// this is OK
		if (DEBUG) {
		    debug("    gc adding " + clist[i]);
		}
	    }

	    if (clunitDB.getExtendSelections() > 0 &&
		    item.getPrevious() != null) {
		ViterbiCandidate lc = (ViterbiCandidate) (item.
		    getPrevious().getFeatures().getObject("clunit_cands"));
		if (DEBUG) {
		    debug("      lc " + lc);
		}
		for (int e = 0; lc != null && 
			(e < clunitDB.getExtendSelections());
			lc = lc.next) {
		    int nu = clunitDB.getNextUnit(lc.ival);
		    if (DEBUG) {
			debug("      e: " + e + " nu: " + nu);
		    }
		    if (nu == ClusterUnitDatabase.CLUNIT_NONE) {
			continue;
		    }

		    for (gt = all; gt != null; gt = gt.next) {
			if (DEBUG) {
			    debug("       gt " + gt.ival + " nu " + nu);
			}
			if (nu == gt.ival) {
			    break;
			}
		    }

		    if (DEBUG) {
			debug("nu " + clunitDB.getUnit(nu).getName() + " all " +
			      clunitDB.getUnit(all.ival).getName() +
			      " " + all.ival);
		    }
		    if ((gt == null)&&clunitDB.isUnitTypeEqual(nu, all.ival)) {
			p = new ViterbiCandidate();
			p.next = all;
			p.item = item;
			p.score = 0;
			p.setInt(nu);
			all = p;
			e++;
		    }
		}
	    }
	    item.getFeatures().setObject("clunit_cands", all);
	    return all;
	}

	/**
	 * Finds the best path.
	 *
	 * @param path the path of interest
	 * @param candiate the candidate of interest
	 *
	 * @return the best path
	 */
	private ViterbiPath getPath(ViterbiPath path, 
		ViterbiCandidate candidate) {
	    int cost;
	    ViterbiPath newPath = new ViterbiPath();

	    newPath.candidate = candidate;
	    newPath.from = path;
	//
	// Flite 1.1 has some logic here to test to see
	// if  the unit database is fully populated or not and if not
	// load fixed residuals and calculate distance with a
	// different distance algorithm that is designed for fixed
	// point. FreeTTS doesn't really need to do that.
	//

	    if (path == null || path.candidate == null) {
		cost = 0;
	    } else {
		int u0 = path.candidate.ival;
		int u1 = candidate.ival;
		if (clunitDB.getOptimalCoupling() == 1) {
		    Cost oCost = getOptimalCouple(u0, u1);
		    if (oCost.u0Move != -1) {
			newPath.setFeature("unit_prev_move", new
				Integer(oCost.u0Move));
		    }
		    if (oCost.u1Move != -1) { 
			newPath.setFeature("unit_this_move", new
				Integer(oCost.u1Move));
		    }
		    cost = oCost.cost;
		} else if (clunitDB.getOptimalCoupling() == 2) {
		    cost = getOptimalCoupleFrame(u0, u1);
		} else {
		    cost = 0;
		}
	    }

	    // cost *= clunitDB.getContinuityWeight();
	    cost *= 5;	// magic number ("continuity weight") from flite
	    newPath.state = candidate.pos;
	    if (path == null) {
		newPath.score = cost + candidate.score;
	    } else {
		newPath.score = cost + candidate.score + path.score;
	    }

	    return newPath;
	}

	/**
	 * Find the best path.
	 *
	 * @return the best path.
	 */
	private ViterbiPath findBestPath() {
	    ViterbiPoint t;
	    int best;
	    int worst;
	    ViterbiPath bestPath = null;

	    if (bigIsGood) {
		worst = Integer.MIN_VALUE;
	    } else {
		worst = Integer.MAX_VALUE;
	    }

	    best = worst;

	    t = lastPoint;

	    if (numStates != 0) {
		if (DEBUG) {
		    debug("fbp ns " + numStates + " t " 
			    + t.numStates + " best " + best);
		}
		for (int i = 0; i < t.numStates; i++) {
		    if (t.statePaths[i] != null && 
			(isBetterThan(t.statePaths[i].score, best))) {
			best = t.statePaths[i].score;
			bestPath = t.statePaths[i];
		    }
		}
	    }
	    return bestPath;
	}

	/**
	 * Gets the cost for a pair of units.
	 *
	 * @param u0  first unit to try
	 * @param u1  second unit to try
	 *
	 * @return the cost
	 */
	Cost getOptimalCouple(int u0, int u1) {
	    int a,b;
	    int u1_p;
	    int i, fcount;
	    int u0_st, u1_p_st, u0_end, u1_p_end;
	    int best_u0, best_u1_p;
	    int dist, best_val; 
	    Cost cost = new Cost();

	    u1_p = clunitDB.getPrevUnit(u1);

	    if (u1_p == u0) {
		return cost;
	    }


	    if (u1_p == ClusterUnitDatabase.CLUNIT_NONE ||
		    clunitDB.getPhone(u0) !=
		    clunitDB.getPhone(u1_p)) {
		cost.cost = 10 * getOptimalCoupleFrame(u0, u1);
		return cost;
	    }


	    u0_end = clunitDB.getEnd(u0) - clunitDB.getStart(u0);
	    u1_p_end = clunitDB.getEnd(u1_p) - clunitDB.getStart(u1_p);
	    u0_st = u0_end / 3;
	    u1_p_st = u1_p_end / 3;

	    if ((u0_end - u0_st) < (u1_p_end - u1_p_st)) {
		fcount = u0_end - u0_st;
	    } else {
		fcount = u1_p_end - u1_p_st;
	    }


	    best_u0 = u0_end;
	    best_u1_p = u1_p_end;
	    best_val = Integer.MAX_VALUE;

	    for (i = 0; i < fcount; ++i) {
		a = clunitDB.getStart(u0)+ u0_st + i;
		b = clunitDB.getStart(u1_p) + u1_p_st + i;
		dist = getFrameDistance(a, b,
		     clunitDB.getJoinWeights(),
		     clunitDB.getMcep().getSampleInfo().getNumberOfChannels())
		      + Math.abs( clunitDB.getSts().getFrameSize(a) - 
			    clunitDB.getSts().getFrameSize(b)) * 
			    clunitDB.getContinuityWeight();

		if (dist < best_val) {
		    best_val = dist;
		    best_u0 = u0_st + i;
		    best_u1_p = u1_p_st + i;
		}
	    }

	    cost.u0Move = clunitDB.getStart(u0) + best_u0;
	    cost.u1Move = clunitDB.getStart(u1_p) + best_u1_p;
	    cost.cost = 30000 + best_val;
	    return cost;
	}

	/** 
	 * Returns the distance between the successive potential
	 * frames.
	 *
	 * @param u0 the first unit to try
	 * @param u1 the second unit to try
	 *
	 * @return the distance between the two units
	 */
	int getOptimalCoupleFrame(int u0, int u1) {
	    int a, b;

	    if (clunitDB.getPrevUnit(u1) == u0) {
		return 0; // consecutive units win
	    }

            if (clunitDB.getNextUnit(u0) != ClusterUnitDatabase.CLUNIT_NONE) {
                a = clunitDB.getEnd(u0);
	    } else {  // don't want to do this but it's all that is left to do
                a = clunitDB.getEnd(u0) - 1; // if num frames < 1 this is bad
            }
            b = clunitDB.getStart(u1);

	    return getFrameDistance(a, b, 
		    clunitDB.getJoinWeights(),
		    clunitDB.getMcep().getSampleInfo().getNumberOfChannels())
		+ Math.abs( clunitDB.getSts().getFrameSize(a) - 
			    clunitDB.getSts().getFrameSize(b)) * 
			    clunitDB.getContinuityWeight();
	}

	/**
	 * Get the 'distance' between the frames a and b.
	 *
	 * @param a first frame
	 * @param b second frame
	 * @param joinWeights the weights used in comparison
	 * @param order number of compares
	 *
	 * @return the distance between the frames
	 */
	public int getFrameDistance(int a, int b, int[] joinWeights,int order) {

	    if (DEBUG) {
		debug(" gfd  a " + a   + " b " + b + " or " + order);
	    }
	    int r, i;
	    short[] bv = clunitDB.getMcep().getSample(b).getFrameData();
	    short[] av = clunitDB.getMcep().getSample(a).getFrameData();

	    for (r = 0, i = 0; i < order; i++) {
		int diff = av[i] - bv[i];
		r += Math.abs(diff) * joinWeights[i] / 65536;
	    }
	    return r;
	}

    }


    /**
     * Represents a point in the Viterbi path.
     */
    static class ViterbiPoint {
	Item item = null;
	int numStates = 0;
	int numPaths = 0;
	ViterbiCandidate cands = null;
	ViterbiPath paths = null;
	ViterbiPath[] statePaths = null;
	ViterbiPoint next = null;

	/**
	 * Creates a ViterbiPoint for the given item.
	 *
	 * @param item the item of interest
	 */
	public ViterbiPoint(Item item) {
	    this.item = item;
	}

	/**
	 * Initialize the path array to the given size.
	 *
	 * @param size the size of the path array
	 */
	public void initPathArray(int size) {
	    if (DEBUG) {
		debug("init_path_array: " + size);
	    }
	    numStates = size;
	    statePaths = new ViterbiPath[size];
	}

	/**
	 * Initializes the dynamic path array.
	 *
	 * @param candidate the candidate of interest
	 */
	public void initDynamicPathArray(ViterbiCandidate candidate) {
	    int i = 0;
	    for (ViterbiCandidate cc = candidate; cc != null; 
		    				i++, cc = cc.next) {
		cc.pos = i;
	    }
	    if (DEBUG) {
		debug("init_dynamic_ path_array: " + i);
	    }
	    initPathArray(i);
	}

	public String toString() {
	    return " pnt: " + numStates + " paths " + numPaths;
	}
    }

    /**
     * Represents a candiate for the Viterbi algorthm.
     */
    static class ViterbiCandidate {
	int score = 0;
	Object value = null;
	int ival = 0;
	int pos = 0;
	Item item = null;
	ViterbiCandidate next = null;

	/**
	 * Sets the object for this candidate.
	 * 
	 * @param obj the object
	 */
	void set(Object obj) {
	    value = obj;
	}

	/**
	 * Sets the integer value  for this candidate.
	 * 
	 * @param ival the integer value
	 */
	void setInt(int ival) {
	    this.ival = ival;
	    set(new Integer(ival));
	}

	/**
	 * Converts this object to a string.
	 *
	 * @return the string form of this object
	 */
	public String toString() {
	    return "VC: Score " + score + " ival " + ival + " Pos " + pos;
	}
    }

    /**
     * Describes a Viterbi path.
     */
    static class ViterbiPath {
	int score = 0;
	int state = 0;
	ViterbiCandidate candidate = null;
	private FeatureSet f = null;
	ViterbiPath from = null;
	ViterbiPath next = null;

	/**
	 * Sets a feature with the given name to the given value.
	 *
	 * @param name the name of the feature
	 * @param value the new value for the feature
	 */
	void setFeature(String name, Object value) {
	    if (f == null) {
		f = new FeatureSetImpl();
	    }
	    f.setObject(name, value);
	}

	/**
	 * Retrieves a feature.
	 *
	 * @param name the name of the feature
	 * 
	 * @return the feature
	 */
	Object getFeature(String name) {
	    Object value = null;
	    if (f != null) {
		value = f.getObject(name);
	    }
	    return value;
	}

	/**
	 * Determines if the feature with the given name
	 * exsists.
	 *
	 * @param name the feature to look for
	 *
	 * @return <code>true</code> if the feature is present;
	 * 	otherwise <code>false</code>.
	 */
	boolean isPresent(String name) {
	    if (f == null) {
		return false;
	    } else {
		return getFeature(name) != null;
	    }
	}

	/**
	 * Converts this object to a string.
	 *
	 * @return the string form of this object
	 */
	public String toString() {
	    return "ViterbiPath score " + score + " state " + state;
	}
    }

    /**
     * Prints debug messages.
     * 
     * @param s the debug message
     */
    static void debug(String s) {
	if (DEBUG) {
	    System.out.println("cludebug: " + s);
	}
    }
}


/**
 * Information returned from getOptimalCoupling.
 */
class Cost {
    int cost = 0;
    int u0Move = -1;
    int u1Move = -1;
}


/**
 * A Cluster Unit.
 */
class ClusterUnit implements com.sun.speech.freetts.Unit {

    private final static boolean DEBUG = false;

    private ClusterUnitDatabase db;
    private String name;
    private int start;
    private int end;

    /**
     * Contructs a cluster unit given.
     *
     * @param db the database
     * @param name the unitName
     * @param start the start
     * @param end the end
     */
    public ClusterUnit(ClusterUnitDatabase db, String name, int start,int end) {
	this.db = db;
	this.start = start;
	this.end = end;
	this.name = name;
    }


    /**
     * Returns the start.
     *
     * @return the start
     */
    public int getStart() {
	return start;
    }

    /**
     * Returns the end.
     *
     * @return the end
     */
    public int getEnd() {
	return end;
    }

    /**
     * Returns the name of this Unit.
     *
     * @return the name of this unit
     */
    public String getName() {
	return name;
    }

    /**
     * returns the size of the unit.
     *
     * @return the size of the unit
     */
    public int getSize() {
	return db.getSts().getUnitSize(start, end);
    }

    /**
     * Retrieves the nearest sample.
     *
     * @param index the ideal index
     *
     * @return the nearest Sample
     */
    public Sample getNearestSample(float index) {
	int i, iSize = 0, nSize;
	SampleSet sts = db.getSts();

	// loop through all the Samples in this unit
	for (i = start; i < end; i++) {
	    Sample sample = sts.getSample(i);
	    nSize = iSize + sample.getResidualSize();

	    if (Math.abs(index - (float) iSize) <
		Math.abs(index - (float) nSize)) {
		return sample;
	    }
	    iSize = nSize;
	}
	return sts.getSample(end - 1);
    }

    /**
     * gets the string name for the unit.
     *
     * @return string rep of this object.
     */
    public String toString() {
	return getName();
    }


    /** 
     * Dumps this unit.
     */
    public void dump()  {
    }

    /**
     * Prints debugging statements.
     *
     * @param s the debugging message
     */
    private void debug(String s) {
	if (DEBUG) {
	    System.out.println("Clunit debug: " + s);
	}
    }
}

