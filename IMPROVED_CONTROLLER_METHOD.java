// SOLUTION 2: Simple regex-based approach (Recommended for your use case)
@GetMapping
public ResponseEntity<List<TimeSlotTemplateDTO>> getAllTimeSlotTemplates() {
    log.info("Getting all time slot templates");
    List<TimeSlotTemplateDTO> templates = timeSlotTemplateService.getAllTimeSlotTemplates();
    
    // Natural sorting for "Hour X" format
    templates.sort((t1, t2) -> {
        String name1 = t1.getName();
        String name2 = t2.getName();
        
        // Extract numbers from the names (assuming format like "Hour 1", "Hour 2", etc.)
        Integer num1 = extractNumber(name1);
        Integer num2 = extractNumber(name2);
        
        // If both have numbers, compare numerically
        if (num1 != null && num2 != null) {
            return Integer.compare(num1, num2);
        }
        
        // Fallback to alphabetical comparison if no numbers found
        return name1.compareToIgnoreCase(name2);
    });
    
    // Log the sorted order for debugging
    log.info("Sorted time slot templates:");
    templates.forEach(template -> log.info("- {}", template.getName()));
    
    return ResponseEntity.ok(templates);
}

/**
 * Extract number from strings like "Hour 1", "Hour 10", etc.
 * Returns null if no number is found.
 */
private Integer extractNumber(String str) {
    if (str == null) return null;
    
    // Look for pattern like "Hour 1", "Period 2", etc.
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(\\d+)\\b");
    java.util.regex.Matcher matcher = pattern.matcher(str);
    
    if (matcher.find()) {
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    return null;
}

// SOLUTION 3: Using the NaturalOrderComparator utility class
@GetMapping
public ResponseEntity<List<TimeSlotTemplateDTO>> getAllTimeSlotTemplatesWithNaturalOrder() {
    log.info("Getting all time slot templates");
    List<TimeSlotTemplateDTO> templates = timeSlotTemplateService.getAllTimeSlotTemplates();
    
    // Use natural order comparator
    NaturalOrderComparator naturalComparator = new NaturalOrderComparator();
    templates.sort((t1, t2) -> naturalComparator.compare(t1.getName(), t2.getName()));
    
    // Log the sorted order for debugging
    log.info("Sorted time slot templates:");
    templates.forEach(template -> log.info("- {}", template.getName()));
    
    return ResponseEntity.ok(templates);
}

// SOLUTION 4: Most robust approach using custom comparator with multiple fallbacks
@GetMapping
public ResponseEntity<List<TimeSlotTemplateDTO>> getAllTimeSlotTemplatesRobust() {
    log.info("Getting all time slot templates");
    List<TimeSlotTemplateDTO> templates = timeSlotTemplateService.getAllTimeSlotTemplates();
    
    templates.sort((t1, t2) -> {
        String name1 = t1.getName();
        String name2 = t2.getName();
        
        if (name1 == null && name2 == null) return 0;
        if (name1 == null) return -1;
        if (name2 == null) return 1;
        
        // Try to extract and compare numbers first
        Integer num1 = extractNumberFromEnd(name1);
        Integer num2 = extractNumberFromEnd(name2);
        
        if (num1 != null && num2 != null) {
            // Both have numbers, compare the prefix first, then the number
            String prefix1 = name1.substring(0, name1.lastIndexOf(num1.toString())).trim();
            String prefix2 = name2.substring(0, name2.lastIndexOf(num2.toString())).trim();
            
            int prefixComparison = prefix1.compareToIgnoreCase(prefix2);
            if (prefixComparison != 0) {
                return prefixComparison;
            }
            
            // Same prefix, compare numbers
            return Integer.compare(num1, num2);
        }
        
        // Fallback to natural string comparison
        return name1.compareToIgnoreCase(name2);
    });
    
    // Log the sorted order for debugging
    log.info("Sorted time slot templates:");
    templates.forEach(template -> log.info("- {}", template.getName()));
    
    return ResponseEntity.ok(templates);
}

/**
 * Extract the last number from a string (more specific for "Hour 1" format)
 */
private Integer extractNumberFromEnd(String str) {
    if (str == null) return null;
    
    // Look for the last number in the string
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)(?!.*\\d)");
    java.util.regex.Matcher matcher = pattern.matcher(str);
    
    if (matcher.find()) {
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    return null;
}
