#!/usr/bin/bash
mv overview-summary.html oldov.html
unzip mecureodoc.zip
mv overview-summary.html newov.html
mv oldov.html overview-summary.html
d2umany $(find -type f -name "*.html")
chmod -R a+r *
chmod a+rx $(find -type d)
