// a Groovy template for generating the object<->xml mappings

type = "Source"

fields = ["id", "sourceType", "year", "volumeNo",
            "firstAuthor", "additionalAuthors",
            "title", "titleOfAnthology", "placeOfPublications",
            "publisher", "issueNo", "journal", "namesOfEditors", "comment"]

// field declarations template
println "// TODO: check the field types, by default these are strings"
fields.each {
     println "private String $it;"
}

println "// TODO: generate the getters and setters for "

// fromXml template            
println """
   static $type fromXml(Element e) {
       if(e == null)
           return null;
       $type ${type.toLowerCase()} = new $type();"""

fields.each {field ->
println """
       // ${type.toLowerCase()}.${field} = In.childText(e, "$field"); 
       ${type.toLowerCase()}.${field} = e.getAttributeValue("$field");
"""
}
        
println """ 
        // pattern for Integer fields
        // String FIELD_Str = In.childText(e, "FIELD"); 
        // String FIELD_Str = e.getAttributeValue("FIELD"); 
        // if(FIELD_Str != null)
        //    ${type.toLowerCase()}.FIELD = In.integer(FIELD_Str); 
         
        return ${type.toLowerCase()};
   } 
"""

// toXml template
println """
    Element toXml() {
        Element element = new Element("${type.toLowerCase()}", Out.NS);
"""
fields.each { field ->
        println"""
            if($field != null)
                // Out.addChild(element, "$field", $field);
                element.setAttribute("$field", $field);
        """
}
println """
        return element;
    }
""" 

