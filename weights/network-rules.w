sibling(X,Y) :- male(X),male(Y).
finalKappa(X) :- sibling(X,Y).
finalLambda :- finalKappa(X).
