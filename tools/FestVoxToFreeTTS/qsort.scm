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
;       Replace the sun_string<? function when the scheme interpretor had
;        a built-in equivalent.  (awb suggested this will happen).
;       


; used to compare two lists by their first elements which are strings
(define (carstring<? e1 e2)
;[[[TODO: sun_string<? replace with string<? when function becomes available in
;  interpreter.]]]
    (sun_string<? (car e1) (car e2))
)

; used to compare two lists by their first elements which are strings
(define (carstring=? e1 e2)
    (string-equal (car e1) (car e2))
)

; quicksort a list l based on two comparison operations < and ==.
; stable sort
(define (qsort l cmp<? cmp=?)
    ; return three lists, a leftpart, the pivotlist and a rightpart
    ; pivot list is a list of elements where element cmp=? pivot
    (define (split l pivot leftlist pivotlist rightlist)
        (cond
            ((null? l)
                (list leftlist pivotlist rightlist))
            ((cmp=? (car l) pivot)
              (split (cdr l) pivot
                leftlist (append pivotlist (list (car l))) rightlist))
            ((cmp<? (car l) pivot)
              (split (cdr l) pivot
                (append leftlist (list (car l))) pivotlist rightlist))
            (t (split (cdr l) pivot
                leftlist pivotlist (append rightlist (list (car l)))))
    ))
    (cond
        ((< (length l) 3) ; base case
         (cond
            ((cdr l) ; if l has two entries
                (if (cmp<? (car l) (cadr l))
                    l
                    (append (cdr l) (list (car l)))))
            (t l))
        )
        (t (let ((pivot (nth (/ (length l) 2) l)))
             (let ((newlists (split l pivot nil nil nil)))
               (append (qsort (car newlists) cmp<? cmp=?)
                    (cadr newlists)
                    (qsort (caddr newlists) cmp<? cmp=?)))))
    )
)

; This function may be implemented by the interpretor in future versions
;[[[TODO: replace used of sun_string<? with string<? when function
; becomes available in interpreter.]]]
(define (sun_string<? str1 str2)
    (define (char->int char)
        (cond
            ((string-equal char "") 45) ((string-equal char "_") 95)
            ((string-equal char "0") 48) ((string-equal char "1") 49)
            ((string-equal char "2") 50) ((string-equal char "3") 51)
            ((string-equal char "4") 52) ((string-equal char "5") 53)
            ((string-equal char "6") 54) ((string-equal char "7") 55)
            ((string-equal char "8") 56) ((string-equal char "9") 57)
            ((string-equal char "?") 63) ((string-equal char "@") 64)
            ((string-equal char "A") 65) ((string-equal char "a") 97)
            ((string-equal char "B") 66) ((string-equal char "b") 98)
            ((string-equal char "C") 67) ((string-equal char "c") 99)
            ((string-equal char "D") 68) ((string-equal char "d") 100)
            ((string-equal char "E") 69) ((string-equal char "e") 101)
            ((string-equal char "F") 70) ((string-equal char "f") 102)
            ((string-equal char "G") 71) ((string-equal char "g") 103)
            ((string-equal char "H") 72) ((string-equal char "h") 104)
            ((string-equal char "I") 73) ((string-equal char "i") 105)
            ((string-equal char "J") 74) ((string-equal char "j") 106)
            ((string-equal char "K") 75) ((string-equal char "k") 107)
            ((string-equal char "L") 76) ((string-equal char "l") 108)
            ((string-equal char "M") 77) ((string-equal char "m") 109)
            ((string-equal char "N") 78) ((string-equal char "n") 110)
            ((string-equal char "O") 79) ((string-equal char "o") 111)
            ((string-equal char "P") 80) ((string-equal char "p") 112)
            ((string-equal char "Q") 81) ((string-equal char "q") 113)
            ((string-equal char "R") 82) ((string-equal char "r") 114)
            ((string-equal char "S") 83) ((string-equal char "s") 115)
            ((string-equal char "T") 84) ((string-equal char "t") 116)
            ((string-equal char "U") 85) ((string-equal char "u") 117)
            ((string-equal char "V") 86) ((string-equal char "v") 118)
            ((string-equal char "W") 87) ((string-equal char "w") 119)
            ((string-equal char "X") 88) ((string-equal char "x") 120)
            ((string-equal char "Y") 89) ((string-equal char "y") 121)
            ((string-equal char "Z") 90) ((string-equal char "z") 122)
	    ((string-equal char ":") 58) ((string-equal char "ä") 253)
	    ((string-equal char "ö") 271)((string-equal char "ü") 277)
	    ((string-equal char "Ä") 221)((string-equal char "Ö") 239)
	    ((string-equal char "Ü") 393)

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

