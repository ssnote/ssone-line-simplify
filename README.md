
# ssone line-simplify

Kotlin implementation of the Ramer Douglas Peucker Algorithm

For more information: https://smallsketch.app/posts/line-simplify-with-ramer-douglas-peucker-algorithm/


## requirements

```
$ kotlinc -version
info: kotlinc-jvm 1.8.10 (JRE 17.0.7+7-Ubuntu-0ubuntu122.04.2)
```

```
$ make -version
GNU Make 4.3
Built for x86_64-pc-linux-gnu
Copyright (C) 1988-2020 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
```


## usage

Do _make_ and get a ./a-coffee-cup.svg in current dir.


## an example

A original coffee cup SVG:

![a-coffee-cup](svg/a-coffee-cup.svg)


A simplified (epsilon = 0.5) coffee cup SVG:

![a-coffee-cup(simplified)](svg/a-coffee-cup_simplified.svg)

