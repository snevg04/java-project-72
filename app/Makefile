# Makefile

.PHONY: build
build:
	./gradlew build

run-dist:
	build/install/app/bin/app

.PHONY: jacoco
report:
	./gradlew jacocoTestReport
	open build/reports/jacoco/test/html/index.html
