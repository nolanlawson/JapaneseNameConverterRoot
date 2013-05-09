Japanese Name Converter
=========

Author
-------
Nolan Lawson

License
-------
[WTFPL][1], although attribution would be nice.

Overview
-------
The Japanese Name Converter is a simple Android app that converts English names into their Japanese equivalents using a mix of dictionary lookup and machine learning. The machine learning algorithm is a variation of the Transformation-Based Learning (TBL) method invented by Eric Brill.  You can read [the explanatory article][2] for more info.

The project is made up of two components: 1) a Maven Java project for building the TBL transduction model (preprocessing phase), and 2) the Android app itself.

Related projects
-------------

* [Japanese Name Converter Server][3] - REST service
* [Japanese Name Converter UI][4] - webapp

[1]: http://sam.zoy.org/wtfpl/
[2]: http://nolanlawson.com/2011/03/30/jnameconverter/
[3]: http://github.com/nolanlawson/jnameconverter-server/
[4]: http://github.com/nolanlawson/japanese-name-converter-ui/
