#!/bin/bash

# Script to create a compressed tar archive of bin and lib directories
# Excludes bin/examples and bin/testfiles
# Archives files under apache-jmeter/ directory

# Generate datetime string in format YYYYMMDD_HHMMSS
DATETIME=$(date +%Y%m%d_%H%M%S)

# Archive name
ARCHIVE_NAME="jmeter_${DATETIME}.tgz"

# Create temporary directory
TEMP_DIR=$(mktemp -d)
trap "rm -rf ${TEMP_DIR}" EXIT

# Create apache-jmeter directory structure
mkdir -p "${TEMP_DIR}/apache-jmeter"

# Copy bin directory excluding examples and testfiles
# Using rsync for better exclusion control
rsync -av --exclude='examples' --exclude='testfiles' \
    bin/ "${TEMP_DIR}/apache-jmeter/bin/"

# Copy lib directory
cp -R lib "${TEMP_DIR}/apache-jmeter/"

# Create the archive from temp directory
# -C: change to directory before archiving
# -c: create archive
# -z: compress with gzip
# -f: specify filename
tar -czf "${ARCHIVE_NAME}" -C "${TEMP_DIR}" apache-jmeter

# Check if tar command succeeded
if [ $? -eq 0 ]; then
    echo "Successfully created archive: ${ARCHIVE_NAME}"
    # Show archive size
    ls -lh "${ARCHIVE_NAME}"
else
    echo "Error: Failed to create archive"
    exit 1
fi
