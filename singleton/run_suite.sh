#!/usr/bin/env bash

# Exit immediately if any command fails
set -e

# Visual Anchors for the Console Output

echo "======================================================="
echo "   Java Singleton Pattern Deep-Dive Performance Suite  "
echo "======================================================="

# Step 1: Check if Java and Maven are installed
echo -e "\n Checking tools..."
if ! which mvn; then
    echo "Error: Maven (mvn) is not installed or not in your PATH."
    exit 1
fi

if ! which java; then
    echo "Error: Java is not installed or not in your PATH."
    exit 1
fi

java -version 2>&1 | head -n 1

# Step 2: Clean the environment to remove stale target builds
echo "\n Cleaning up previous build artifacts..."
mvn clean

# Step 3: Compile and run the complete test/benchmark architecture
echo "\n Compiling codebase & executing test suites..."
echo "(This runs thread concurrency benchmarks, reflection attacks, and serialization tests)\n"

mvn test

echo "\n Execution Completed Successfully! Check logs above for benchmark timings."
echo "======================================================="