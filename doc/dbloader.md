# DBLoader

DBLoader is the base underlying format behind the old databases.

DBLoader is separated into lines. Each line is terminated by a newline (but there are exceptions when a newline won't count).

The first character of the line determines the type. If the first character is whitespace, the line is considered a comment.

If the first character is a digit (0 through 9), then the whole line is read as a single string. `:` is expected to be found. The area before is converted to an integer. The area after is trimmed and given directly.

Outside of this case, the line is tokenized, which works like this (in order):

* EOF finishes the tokenization

* Grave accent toggles "block quote mode" and finishes that character's processing

* If in block quote mode, append to token buffer and finish that character's processing

* Newline finishes the tokenization

* Whitespace/control characters finish the current token if the token buffer is non-empty, otherwise do nothing, finishing character processintg in any case

* If at the start of a token, `"` begins a JSON-style string (using its own set of rules). Note however that the end of the string doesn't finish the token.

* Anything else is appended to the token buffer.

Notable historical notes:

* The JSON-style string was a major thing when it was added, because it allowed removal of "EscapedStringSyntax", a workaround for the lack of ability to write certain characters in SDB.

* The block quote mode was added so that inline Datum code wouldn't need to escape string quotes.


