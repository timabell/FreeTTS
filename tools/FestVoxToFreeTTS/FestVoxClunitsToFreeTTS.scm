; David Vos
; Modeled on flite/tools/make_clunits.scm
; Portions Copyright 2003 Sun Microsystems, Inc.
; Portions Copyright 1999-2003 Language Technologies Institute,
; Carnegie Mellon University.
; All Rights Reserved.  Use is subject to license terms.
;
; See the file "license.terms" for information on usage and
; redistribution of this file, and for a DISCLAIMER OF ALL
; WARRANTIES.
;
; Note: The variable t is the value true, but casts to the string "t" and is
;        used that way in places.  nil is false.
;       I think consp is the list? function.
;       I am not positive the dump_cart function is correct
;       Replace the sun_string<? function when the scheme interpretor had
;        a built-in equivalent.  (awb suggested this will happen).
;       
; Convert festvox voice to FreeTTS

(define (dump_clunits name voicedir outdir misc_filename unit_type_filename cart_filename units_filename lpc_filename lpc_header_filename mcep_filename mcep_header_filename join_weights_filename)
    (load (format nil "%s/festival/trees/%s.tree" voicedir name))
    (let (
          (misc_file (fopen (format nil "%s/%s" outdir misc_filename) "w"))
          (unit_type_file (fopen
                    (format nil "%s/%s" outdir unit_type_filename) "w"))
          (cart_file (fopen (format nil "%s/%s" outdir cart_filename) "w"))
          (units_file (fopen (format nil "%s/%s" outdir units_filename) "w"))
          (lpc_file (fopen (format nil "%s/%s" outdir lpc_filename) "w"))
          (lpc_header_file
                    (fopen (format nil "%s/%s" outdir lpc_header_filename) "w"))
          (join_weights_file (fopen
                    (format nil "%s/%s" outdir join_weights_filename) "w"))
          (lpcdir (string-append voicedir "/sts"))
          (mcepdir (string-append voicedir "/mcep"))
          (mcep_file (fopen (format nil "%s/%s" outdir mcep_filename) "w"))
          (mcep_header_file (fopen
                    (format nil "%s/%s" outdir mcep_header_filename) "w"))
          (clindex (load
                    (format nil "%s/festival/clunits/%s.fileordered.scm"
                     voicedir name) t))
          (clcatfnunitordered 
                    (format nil "%s/festival/clunits/%s.unitordered.scm"
                     voicedir name))
          (sorted_clunits_selection_trees (qsort-tree clunits_selection_trees))
       )

        ; compile some general data
        (format t "Writing misc data.\n")
        (dump_misc misc_file)
        (format t "Writing carts.\n")
        (dump_carts cart_file sorted_clunits_selection_trees)
        (format t "Writing join weights.\n")
        (dump_join_weights join_weights_file)

        ; Build clunits_entries, and output lpc and mcep data
        (format t "Calculating lpc and mcep data.\n")
        (set! clunits_entries nil)
        (set! pm_pos 0)
        (load (string-append mcepdir "/mcep.params.scm"))
        (while clindex
            (set! pms (find_pm_pos 
                name
                (car clindex)
                lpcdir
                mcepdir
                lpc_file
                mcep_file
            ))
            (set! clunits_entries
                (cons
                 (list 
                  (nth 0 (car clindex))
                  (nth 2 pms) ; start_pm
                  (nth 3 pms) ; phone_boundary_pm
                  (nth 4 pms) ; end_pm
                  (nth 5 (car clindex))
                  (nth 6 (car clindex))
                 )
                clunits_entries))
            (set! clindex (cdr clindex))
        )
        (set! clunits_entries
            (sort_clentries clunits_entries clcatfnunitordered))

        ; Output lpc header
        (format t "Writing lpc header.\n")
        (format lpc_header_file "***  sts STS \n")
        (format lpc_header_file "STS STS %d %d %d %f %f %f %d\n"
            pm_pos          ; number of lpc entries
            lpc_order
            sample_rate
            lpc_min
            lpc_range
            0.0             ; post emphasis
            1               ; residual fold
        )

        ; Output mcep header
        (format t "Writing mcep header.\n")
        (format mcep_header_file "***  sts MCEP \n")
        (format mcep_header_file "STS MCEP %d %d %d %f %f %f %d\n"
            pm_pos          ; number of mcep entries
            mcep_order
            sample_rate
            mcep_min
            mcep_range
            0.0             ; post emphasis
            1               ; residual fold
        )

        (format t "Building phoneme, unit, and unit_type dictionaries.\n")
        ; match phonemes to integer id's
        (set! phone_count 0)
        (set! phonemes nil)
        (mapcar
         (lambda (p)
           (if (not (string-matches (car p) ".*#.*"))
                (set! phonemes (cons (list (car p) phone_count) phonemes)))
           (set! phone_count (+ 1 phone_count)))
        (cadr (car (PhoneSet.description '(phones)))))

        ; match units to integer id's
        ; CLUNIT_NONE is #define'd to be 65535 in flite
        ;   In this case, it signifies a "null" value
        (set! unit_id_count -1)
        (set! unit_ids (cons (list "CLUNIT_NONE" 65535) (mapcar (lambda (e)
            (set! unit_id_count (+ 1 unit_id_count))
            (list (nth 0 e) unit_id_count)
        )clunits_entries)))

        ; match unit types to integer id's
        (set! unit_types_count -1)
        (set! unittypes (mapcar (lambda (e)
            (set! unit_types_count (+ 1 unit_types_count))
            (list (car e) unit_types_count)
        )sorted_clunits_selection_trees))

        (format t "Writing units data.\n")
        (set! ut_count 0)     ; total instances of unit types encountered
        (set! this_ut_count 0); total instances of current unit type encountered
        (set! this_ut "")     ; undefined
        (format unit_type_file "*** unit types \n")
        (format units_file "***  units \n")
        (mapcar (lambda (e)
           ; print unit_types
           (set! tmp_ut (unit_type (nth 0 e))) ; get the unit_type
           (if (not (string-equal this_ut tmp_ut))
                (begin
                    (if (> ut_count 0)
                        (format unit_type_file "%d\n" this_ut_count))
                    (format unit_type_file "UNIT_TYPE %s %d " tmp_ut ut_count)
                    (set! this_ut_count 0)
                    (set! this_ut tmp_ut)
            ))
           (set! this_ut_count (+ 1 this_ut_count))
           (set! ut_count (+ 1 ut_count))

           ; print units data
           (format units_file "UNITS %d %d %d %d %d %d\n"
              (get_id (unit_type (nth 0 e)) unittypes)            ; type
              (get_id (string-before (nth 0 e) "_") phonemes)     ; phone
              (nth 1 e)                                           ; start_pm
              (nth 3 e)                                           ; end_pm
              (get_id (string-after (nth 4 e) "unit_") unit_ids)  ; start
              (get_id (string-after (nth 5 e) "unit_") unit_ids)  ; end
           ))
        clunits_entries)

        ; finish off last unit_type
        (format unit_type_file "%d\n" this_ut_count)

        (fclose misc_file)
        (fclose unit_type_file)
        (fclose cart_file)
        (fclose lpc_file)
        (fclose lpc_header_file)
        (fclose mcep_file)
        (fclose mcep_header_file)
        (fclose join_weights_file)
    )
)

(define (dump_misc misc_file)
    (format misc_file "CONTINUITY_WEIGHT 100\n") ; f0 weight
    (format misc_file "OPTIMAL_COUPLING 1\n")
    (format misc_file "EXTEND_SELECTIONS 5\n")
)

(define (dump_carts cart_file sorted_clunits_selection_trees)
    (format cart_file "***  carts \n")
    (mapcar
        (lambda (cart)
            (set! current_node 0)
            (let ((tree (cadr cart))
                  (name (car cart)))
              (format cart_file "CART %s %d\n" name (length tree))
              (format cart_file "%s" (print_cart_nodes tree))
            )
        )
    sorted_clunits_selection_trees)
)

(define (dump_join_weights join_weights_file)
    ; hard-coded values
    (format join_weights_file "***  join_weights \n")
    (format join_weights_file "JOIN_WEIGHTS 18 ")
    (format join_weights_file "32768 32768 32768 32768 32768 32768 32768 ")
    (format join_weights_file "32768 32768 32768 32768 32768 32768 32768 ")
    (format join_weights_file "32768 32768 32768 32768 \n")
)

; [[TODO]] make more efficient!! O(n), currently
; idea: presort list, and do binary search
; hash table would be best....
(define (get_id name data)
    (cond
        ((null? data) 65535) ; Not Found
        ((string-equal name (caar data))
            (car (cdr (car data)))
        )
        (t (get_id name (cdr data)))
    )
)

(define (find_pm_pos name entry lpcdir mcepdir lpc_file mcep_file)
  "(find_pm_pos entry lpddir)
Diphone dics give times in seconds here we want them as indexes.  This
function converts the lpc to ascii and finds the pitch marks that
go with this unit.  These are written to lpc_file with ulaw residual
as short term signal."
  (let ((sts_coeffs (load
		     (format nil "%s/%s.sts" lpcdir (cadr entry))
		     t))
 	(mcep_coeffs (load_ascii_track
		      (format nil "%s/%s.mcep" mcepdir (cadr entry))
		      (nth 2 entry)))
	(start_time (nth 2 entry))
	(phoneboundary_time (nth 3 entry))
	(end_time (nth 4 entry))
	start_pm pb_pm end_pm)
    (format t "%l\n" entry) ; output as we convert
    (set! sts_info (car sts_coeffs))
    (set! sts_coeffs (cdr sts_coeffs))
    (while (and sts_coeffs (cdr sts_coeffs)
	    (> (absdiff start_time (car (car sts_coeffs)))
	      (absdiff start_time (car (cadr sts_coeffs)))))
     (set! sts_coeffs (cdr sts_coeffs)))
    (set! sample_rate (nth 2 sts_info))
    (set! lpc_order (nth 1 sts_info))
    (set! lpc_min (nth 3 sts_info))
    (set! lpc_range (nth 4 sts_info))
    (set! start_pm pm_pos)
    (while (and sts_coeffs (cdr sts_coeffs)
	    (> (absdiff phoneboundary_time (car (car sts_coeffs)))
	       (absdiff phoneboundary_time (car (cadr sts_coeffs)))))
        (output_sts (car sts_coeffs) lpc_file)
        (output_mcep (car mcep_coeffs) mcep_file)
        (set! pm_pos (+ 1 pm_pos))
     (set! sts_coeffs (cdr sts_coeffs)))
    (set! pb_pm pm_pos)
    (while (and sts_coeffs (cdr sts_coeffs)
	    (> (absdiff end_time (car (car sts_coeffs)))
	       (absdiff end_time (car (cadr sts_coeffs)))))
        (output_sts (car sts_coeffs) lpc_file)
        (output_mcep (car mcep_coeffs) mcep_file)
        (set! pm_pos (+ 1 pm_pos))
     (set! sts_coeffs (cdr sts_coeffs)))
    (set! end_pm pm_pos)

    (list 
     (car entry)
     (cadr entry)
     start_pm
     pb_pm
     end_pm)))

(define (load_ascii_track trackfilename starttime)
   "(load_ascii_track trackfilename)
Coverts trackfilename to simple ascii representation."
   (let ((tmpfile (make_tmp_filename))
	 (nicestarttime (if (> starttime 0.100)
			    (- starttime 0.100)
			    starttime))
	 b)
     (system (format nil "$ESTDIR/bin/ch_track -otype est -start %f %s | 
                        awk '{if ($1 == \"EST_Header_End\")
                                 header=1;
                              else if (header == 1)
                                 printf(\"( %%s )\\n\",$0)}'>%s" 
		     nicestarttime trackfilename tmpfile))
     (set! b (load tmpfile t))
     (delete-file tmpfile)
     b))

(define (absdiff a b)
  (let ((d (- a b )))
    (if (< d 0)
	(* -1 d)
	d)))

(define (output_sts frame lpc_file)
  "(output_sts frame lpc_file)
Ouput this LPC frame."
  (let ((time (nth 0 frame))
	(coeffs (nth 1 frame))
	(r (nth 3 frame)))

    ; Frames
    (format lpc_file "FRAME ")
    (if (not (null? coeffs)) (format lpc_file "%d " (car coeffs)))
    (while (cdr coeffs)
     (set! coeffs (cdr coeffs))
     (format lpc_file "%d " (car coeffs)))
    (format lpc_file "\n")

    ; Residuals
    (format lpc_file "RESIDUAL %d " (length r))
    (if (not (null? coeffs)) (format lpc_file "%d " (car r)))
    (while (cdr r)
     (set! r (cdr r))
     (format lpc_file "%d " (car r)))
    (format lpc_file "\n")
))

(define (output_mcep frame mcep_file)
  "(output_mcep frame mcep_file)
Ouput this MCEP frame."
  (let ()
    (set! mcep_order (- (length frame) 3))

    (format mcep_file "FRAME ")
    (set! frame (cddr frame)) ;; skip the "1"
    (set! frame (cdr frame)) ;; skip the energy
    (if (not (null? frame))
        (format mcep_file "%d " (mcepcoeff_norm (car frame))))
    (while (cdr frame)
     (set! frame (cdr frame))
     (format mcep_file "%d " (mcepcoeff_norm (car frame))))
    (format mcep_file "\nRESIDUAL 0 \n") ; hard-code residuals
))

(define (mcepcoeff_norm c)
  (* (/ (- c mcep_min) (- mcep_max mcep_min))
     65535))

(define (print_cart_list l)
    (cond
        ((null? l))
        ((cdr l) (format nil "%f %s" (caar l) (print_cart_list (cdr l))))
        (t (format nil "%f" (caar l)))
))

; [[TODO:]] flite/tools/make_cart.scm does not handle lists, therefore
;               I do not because I don't know what format they might be in
(define (print_cart_nodes tree)
    (set! current_node (+ 1 current_node))
    (cond
        ((cdr tree) ;node (non-leaf)
         (let ((operator (cadr (assoc_string (cadr (car tree)) cart_operators)))
               (val (nth 2 (car tree))))
           (let ((type (cond
               ((string-equal operator "=") (format nil "String(%s)" val))
               ((string-equal operator "REGEX") (format nil "Integer(%d)" val))
               ((number? val) (format nil "Float(%f)" val))
               ((consp val) (format stderr "List vals not supported here yet\n")
                  (error val))
               (t (format nil "String(%s)" val))
             )))
         ; It looks like flite does an infix-like way of calculating the
         ;  current_node, but it does a prefix-like way of printing the nodes.
         (let ((left_val (print_cart_nodes (car (cdr tree)))))
          (let ((this_node_val (format nil "NODE %s %s %s %d\n"
                            (caar tree) ;feat
                            operator
                            type
                            current_node
               )))
           (let ((right_val (print_cart_nodes (car (cdr (cdr tree))))))
            (string-append this_node_val left_val right_val))))
        )))
        (t (cond
            ((consp (caar tree))    ;leaf = (caar tree)
             (format nil "LEAF List(%s)\n"
                (print_cart_list (caar tree)))
            )
            (t (format stderr "Unknown leaf format\n") (error 1))
        ))
    )
)

;helper
(defvar cart_operators
  '(("is" "=")
    ("in" "IN")
    ("<" "<")
    (">" ">")
    ("matches" "REGEX")
    ("=" "EQUALS"))) ; CST_CART_OP_EQUALS not handled in
                     ;    Flite->FreeTTS Conversion
                     ; May cause problems.

;helper
(define (unit_type u)
  (apply
   string-append
   (reverse
    (symbolexplode 
     (string-after 
      (apply
       string-append
       (reverse (symbolexplode u)))
      "_")))))

;helper
(define (sort_clentries entries clcatfnunitorder)
  (let ((neworder nil))
    (mapcar
     (lambda (unit)
       (set! neworder (cons (assoc_string (car unit) entries)
			    neworder)))
     (load clcatfnunitorder t))
    (reverse neworder)))

(define (qsort-tree tree)
    ; return two lists, a leftpart and a rightpart
    ; the pivot is not in either list.  (Assumes unique names)
    (define (split tree pivotstr leftlist rightlist)
        (cond
            ((null? tree)
             (list leftlist rightlist))
            ((string-equal (caar tree) pivotstr)
             (split (cdr tree) pivotstr leftlist rightlist))
            ((sun_string<? (caar tree) pivotstr)
             (split (cdr tree) pivotstr (cons (car tree) leftlist) rightlist))
            (t (split (cdr tree) pivotstr leftlist (cons (car tree) rightlist)))
    ))
    (cond  ; base case
        ((< (length tree) 3)
            (cond
                ((cdr tree)
                  (if (sun_string<? (caar tree) (caar (cdr tree)))
                        tree
                        (append (cdr tree) (list (car tree)))))
                (t tree)
        ))
        (t (let ((pivot (nth (/ (length tree) 2) tree)))
            (let ((newlists (split tree (car pivot) nil nil)))
                (append (qsort-tree (car newlists))
                    (list pivot)
                    (qsort-tree (cadr newlists))))))
    )
)

; This function may be implemented by the interpretor in future versions
(define (sun_string<? str1 str2)
    (define (char->int char)
        (cond
            ((string-equal char "_") 10) ((string-equal char "-") 11)
            ((string-equal char "0") 20) ((string-equal char "1") 21)
            ((string-equal char "2") 22) ((string-equal char "3") 23)
            ((string-equal char "4") 24) ((string-equal char "5") 25)
            ((string-equal char "6") 26) ((string-equal char "7") 27)
            ((string-equal char "8") 28) ((string-equal char "9") 29)
            ((string-equal char "A") 50) ((string-equal char "a") 50)
            ((string-equal char "B") 51) ((string-equal char "b") 51)
            ((string-equal char "C") 52) ((string-equal char "c") 52)
            ((string-equal char "D") 53) ((string-equal char "d") 53)
            ((string-equal char "E") 54) ((string-equal char "e") 54)
            ((string-equal char "F") 55) ((string-equal char "f") 55)
            ((string-equal char "G") 56) ((string-equal char "g") 56)
            ((string-equal char "H") 57) ((string-equal char "h") 57)
            ((string-equal char "I") 58) ((string-equal char "i") 58)
            ((string-equal char "J") 59) ((string-equal char "j") 59)
            ((string-equal char "K") 60) ((string-equal char "k") 60)
            ((string-equal char "L") 61) ((string-equal char "l") 61)
            ((string-equal char "M") 62) ((string-equal char "m") 62)
            ((string-equal char "N") 63) ((string-equal char "n") 63)
            ((string-equal char "O") 64) ((string-equal char "o") 64)
            ((string-equal char "P") 65) ((string-equal char "p") 65)
            ((string-equal char "Q") 66) ((string-equal char "q") 66)
            ((string-equal char "R") 67) ((string-equal char "r") 67)
            ((string-equal char "S") 68) ((string-equal char "s") 68)
            ((string-equal char "T") 69) ((string-equal char "t") 69)
            ((string-equal char "U") 70) ((string-equal char "u") 70)
            ((string-equal char "V") 71) ((string-equal char "v") 71)
            ((string-equal char "W") 72) ((string-equal char "w") 72)
            ((string-equal char "X") 73) ((string-equal char "x") 73)
            ((string-equal char "Y") 74) ((string-equal char "y") 74)
            ((string-equal char "Z") 75) ((string-equal char "z") 75)
            (t 255)
        )
    )
    (define (char<? char1 char2)
        (< (char->int char1) (char->int char2))
    )
    (define (charl<? charl1 charl2)
        (cond
            ((null? charl2) nil) ; return false
            ((null? charl1) t)   ; return true
            (t (if (string-equal (car charl1) (car charl2))
                    (charl<? (cdr charl1) (cdr charl2))
                    (char<? (car charl1) (car charl2))))
    ))
    (charl<? (symbolexplode str1) (symbolexplode str2))
)

