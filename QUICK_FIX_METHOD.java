@GetMapping
public ResponseEntity<List<TimeSlotTemplateDTO>> getAllTimeSlotTemplates() {
    log.info("Getting all time slot templates");
    List<TimeSlotTemplateDTO> templates = timeSlotTemplateService.getAllTimeSlotTemplates();
    
    // QUICK FIX: Natural sorting for "Hour X" format
    templates.sort((t1, t2) -> {
        String name1 = t1.getName();
        String name2 = t2.getName();
        
        // Extract numbers using regex
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher m1 = pattern.matcher(name1);
        java.util.regex.Matcher m2 = pattern.matcher(name2);
        
        if (m1.find() && m2.find()) {
            // Both have numbers, compare numerically
            int num1 = Integer.parseInt(m1.group());
            int num2 = Integer.parseInt(m2.group());
            return Integer.compare(num1, num2);
        }
        
        // Fallback to alphabetical if no numbers
        return name1.compareToIgnoreCase(name2);
    });
    
    // Debug output
    for (TimeSlotTemplateDTO template : templates) {
        System.out.println(template.getName());
    }
    
    return ResponseEntity.ok(templates);
}
