; Modeled on flite/tools/make_didb.scm.scm
; Portions Copyright 2003 Sun Microsystems, Inc.
; Portions Copyright 1999-2003 Language Technologies Institute,
; Carnegie Mellon University.
; All Rights Reserved.  Use is subject to license terms.
;
; See the file "license.terms" for information on usage and
; redistribution of this file, and for a DISCLAIMER OF ALL
; WARRANTIES.
;
; Convert festvox voice to FreeTTS

; I'm not sure why these are hard-coded here, but I think they should
; be left that way unless changed in the equivalent flite program
(defvar lpc_min -7.992630)
(defvar lpc_max  7.829990)

; Dump a diphone voice to text
(define (dump_diphone name voicedir outdir header_filename data_filename diphindexfn)
  (let ((diphindex (load diphindexfn t))
        (header_file (fopen (format nil "%s/%s" outdir header_filename) "w"))
        (data_file (fopen (format nil "%s/%s" outdir data_filename) "w"))
        (stsdir (string-append voicedir "/sts"))
       )

    (set! pm_pos 0)

    ; Print data
    (while diphindex
     (set! pms (find_pm_pos 
        (car diphindex)
        stsdir
        data_file))

        (format data_file "DIPHONE %s %d %d %d\n"
            (nth 0 pms)
            (nth 2 pms)
            (nth 3 pms)
            (nth 4 pms))
        (print_data (nth 1 pms) data_file)
    (set! diphindex (cdr diphindex)))

    ; Print header
    (format header_file "NAME %s\n" name)
    (format header_file "SAMPLE_RATE %d\n" sample_rate)
    (format header_file "NUM_CHANNELS %d\n" lpc_order)
    (format header_file "COEFF_MIN %f\n" lpc_min)
    (format header_file "COEFF_RANGE %f\n" lpc_range)

    (fclose header_file)
    (fclose data_file)
))

; Print the data for an individual diphone
(define (print_data entries data_file)
    (cond 
     (entries (cond
        ((string-equal (caar entries) "frame")
            (format data_file "FRAME     ")
            (print_nums (cdr (car entries)) data_file)
        )
        ((string-equal (caar entries) "residual")
            (format data_file "RESIDUAL %d     " (cadr (car entries)))
            (print_nums (cddr (car entries)) data_file)
        ))
      (print_data (cdr entries) data_file)
    ))
)

; Recursively print a list of integers to data file, terminating with a newline
(define (print_nums numlist data_file)
    (cond (numlist
            (format data_file "%d " (car numlist))
            (print_nums (cdr numlist) data_file)
        )
        (t (format data_file "\n"))
    )
)


(define (find_pm_pos entry stsdir)
  "(find_pm_pos entry stsdir)
Diphone dics give times in seconds here we want them as indexes.  This
function converts the lpc to ascii and finds the pitch marks that
go with this unit.  These are written with ulaw residual
as short term signal."
  (let ((sts_coeffs (load
		     (format nil "%s/%s.sts" stsdir (cadr entry))
		     t))
	(start_time (nth 2 entry))
	(phoneboundary_time (nth 3 entry))
	(end_time (nth 4 entry))
	start_pm pb_pm end_pm)
    (format t "%l\n" entry)
    (set! outlist nil)
    (set! sts_info (car sts_coeffs))
    (set! sts_coeffs (cdr sts_coeffs))
    (while (and sts_coeffs
	    (> (absdiff start_time (car (car sts_coeffs)))
	      (absdiff start_time (car (cadr sts_coeffs)))))
     (set! sts_coeffs (cdr sts_coeffs)))
    (set! sample_rate (nth 2 sts_info))
    (set! lpc_order (nth 1 sts_info))
    (set! lpc_min (nth 3 sts_info))
    (set! lpc_range (nth 4 sts_info))
    (set! start_pm pm_pos)
    (while (and sts_coeffs
	    (> (absdiff phoneboundary_time (car (car sts_coeffs)))
	       (absdiff phoneboundary_time (car (cadr sts_coeffs)))))
     (output_sts (car sts_coeffs))
     (set! sts_coeffs (cdr sts_coeffs)))
    (set! pb_pm pm_pos)
    (while (and sts_coeffs (cdr sts_coeffs)
	    (> (absdiff end_time (car (car sts_coeffs)))
	       (absdiff end_time (car (cadr sts_coeffs)))))
     (output_sts (car sts_coeffs))
     (set! sts_coeffs (cdr sts_coeffs)))
    (set! end_pm pm_pos)

    (list 
     (car entry)
     (reverse outlist) ;was (cadr entry) in awb code.
     start_pm
     pb_pm
     end_pm)))

(define (output_sts frame)
  "(output_sts frame)
Ouput this LPC frame."
  (let ((time (nth 0 frame))
	(coeffs (nth 1 frame))
	(size (nth 2 frame))
	(r (nth 3 frame)))

    ; Build frame
    (set! framevals nil)
    (while (cdr coeffs)
     (set! framevals (cons (car coeffs) framevals))
     (set! coeffs (cdr coeffs))
     (if (not (cdr coeffs)) (set! framevals (cons (car coeffs) framevals))))
    (set! outlist (cons (cons "frame" (reverse framevals)) outlist))

    ; Build residual
    (set! resvals nil)
    (while (cdr r)
     (set! resvals (cons (car r) resvals))
     (set! r (cdr r))
     (if (not (cdr r)) (set! resvals (cons (car r) resvals))))
    (set! outlist
        (cons (cons "residual" (cons size (reverse resvals))) outlist))

    (set! pm_pos (+ 1 pm_pos))
))

(define (absdiff a b)
  (let ((d (- a b )))
    (if (< d 0)
	(* -1 d)
	d)))
