#!/bin/bash

# HOW TO USE IT
# ./is-wordpress [domain]
# EXAMPLE
# ./is-wordpress https://blog.us.playstation.com/
# PRINT true

# Globals variables
DOMAIN="$1"
method1=false
method2=false
method3=false

##
############## Method 1: searching in robots.txt
##

# Local ariables
ROBOTS_TXT="$(curl -L -s $DOMAIN/robots.txt)"
SEARCH_DISALLOW="wp-admin"
DISALLOW_STRING="Disallow:"

# Check if contains string: Disallow: wp-admin
if [[ ${ROBOTS_TXT,,} == *${DISALLOW_STRING,,}*${SEARCH_DISALLOW,,}* ]]; then
    method1=true
fi

##
############## Method 2: Search for the test cookie on the login page
##

# Local variables
HEADERS_LOGIN="$(curl -L -s -I $DOMAIN/wp-login.php)"
COOKIE_SEARCH_LOGIN="set-cookie: wordpress_test_cookie=WP+Cookie+check"

# Check if contains cookie in HEADERS, ignore case
if [[ ${HEADERS_LOGIN,,} == *${COOKIE_SEARCH_LOGIN,,}* ]]; then
    method2=true
fi

##
############## Method 3: Search in HTML the WordPress Meta Generator
##

# Local vriables
HTML="$(curl -L -s $DOMAIN)"
REGEX_SEARCH_GENERATOR="<meta.*generator.*wordpress.*>"

# Check if contains meta generator WordPress
if [[ ${HTML,,} =~ $REGEX_SEARCH_GENERATOR ]]; then
    method3=true
fi

##
############## Echo result
##
if $method1 || $method2 || $method3 ; then
    echo true
else
    echo false
fi
