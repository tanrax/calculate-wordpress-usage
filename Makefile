.DEFAULT_GOAL := help
help:
	@perl -nle'print $& if m{^[a-zA-Z_-]+:.*?## .*$$}' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-25s\033[0m %s\n", $$1, $$2}'

prepare: ## Download and format csv with domains 
		touch top-1m.csv
		rm top-1m.csv
		wget http://s3.amazonaws.com/alexa-static/top-1m.csv.zip
		unzip top-1m.csv.zip
		rm top-1m.csv.zip
		cat top-1m.csv | sed -n 's/$$/,nil/p' >> top-1m-temp.csv
		mv top-1m-temp.csv resources/top-1m.csv
		rm top-1m.csv

run: ## Run checks 
		lein run

calculate: ## Calculate the percentage
		(awk 'END {print NR}' resources/top-1m.csv) / (cat resources/top-1m.csv | grep ',true' | wc -l)
