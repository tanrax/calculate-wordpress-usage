#!/bin/bash
CSV="resources/top-1m.csv"
NUM_WORDPRESS=$(cat $CSV | grep ,true$ | wc -l)
NUM_NOT_WORDPRESS=$(cat $CSV | grep ,false$ | wc -l)
NUM_TIMEOUT=$(cat $CSV | grep ,timeout$ | wc -l)
TOTAL=$(cat $CSV | wc -l)

echo "($NUM_WORDPRESS * 100) / ($TOTAL - $NUM_TIMEOUT)" | bc | awk '{print $1"%"}'
