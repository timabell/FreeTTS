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
            ((string-equal char "-") 10) ((string-equal char "_") 11)
            ((string-equal char "0") 20) ((string-equal char "1") 21)
            ((string-equal char "2") 22) ((string-equal char "3") 23)
            ((string-equal char "4") 24) ((string-equal char "5") 25)
            ((string-equal char "6") 26) ((string-equal char "7") 27)
            ((string-equal char "8") 28) ((string-equal char "9") 29)
            ((string-equal char "?") 48) ((string-equal char "@") 49)
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

