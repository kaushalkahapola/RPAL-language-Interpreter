# Variables
SRC_DIR := .
BIN_DIR := bin
MAIN_CLASS := myrpal

# Find all .java files
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Replace .java with .class for the BIN_DIR
CLASSES := $(patsubst $(SRC_DIR)/%.java,$(BIN_DIR)/%.class,$(SOURCES))

# Default target
all: $(BIN_DIR) $(CLASSES)

# Create the bin directory if it doesn't exist
$(BIN_DIR):
	mkdir -p $(BIN_DIR)

# Rule to compile .java to .class
$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	javac -d $(BIN_DIR) -sourcepath $(SRC_DIR) $<

# Clean up the bin directory
clean:
	rm -rf $(BIN_DIR)

# Run the main class
run: all
	java -cp $(BIN_DIR) $(MAIN_CLASS) sample.txt

.PHONY: all clean run
