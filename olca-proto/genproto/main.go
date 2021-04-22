package main

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"unicode"

	yaml "gopkg.in/yaml.v2"
)

// TypeDef contains the defenition of a class or enumeration.
type TypeDef struct {
	Class *ClassDef `yaml:"class"`
	Enum  *EnumDef  `yaml:"enum"`
}

func (def *TypeDef) String() string {
	if def.Class != nil {
		return "ClassDef " + def.Class.Name
	}
	if def.Enum != nil {
		return "EnumDef " + def.Enum.Name
	}
	return "Unknown TypeDef"
}

func (def *TypeDef) name() string {
	if def.Class != nil {
		return def.Class.Name
	}
	if def.Enum != nil {
		return def.Enum.Name
	}
	return "Unknown"
}

// ClassDef contains the definition of a class.
type ClassDef struct {
	Name       string      `yaml:"name"`
	SuperClass string      `yaml:"superClass"`
	Doc        string      `yaml:"doc"`
	Fields     []*FieldDef `yaml:"properties"`
}

// FieldDef is a field (property) declaration of a class.
type FieldDef struct {
	Name string `yaml:"name"`
	Type string `yaml:"type"`
	Doc  string `yaml:"doc"`
}

// EnumDef contains the definition of an enumeration.
type EnumDef struct {
	Name  string      `yaml:"name"`
	Doc   string      `yaml:"doc"`
	Items []*EnumItem `yaml:"items"`
}

// EnumItem is an item of an enumeration definition.
type EnumItem struct {
	Name string `yaml:"name"`
	Doc  string `yaml:"doc"`
}

// ProtoHeader is the file header that is written to the
// generated proto3 file. This is the place where you want
// to define global options
const ProtoHeader = `// Generated from olca-schema (https://github.com/GreenDelta/olca-schema).
// DO NOT EDIT!

syntax = "proto3";

package protolca;

option java_package = "org.openlca.proto.generated";
option java_outer_classname = "Proto";
option csharp_namespace = "ProtoLCA";
option go_package = ".;protolca";

import "entity_type.proto";

`

// BytesHint is a comment we add to fields with `bytes` as data type.
const BytesHint = `  // When we map to the bytes type it means that we have no matching message
  // type and just put the raw bytes into the field. This is specifically true
  // for our geometry data of locations which cannot be translated to valid
  // GeoJSON using Protocol Buffers (as they do not support arrays of arrays).
  // To indicate that this is a different field than the field in the
  // olca-schema definition, we append the _bytes suffix to the field name
`

func main() {

	if len(os.Args) < 2 {
		fmt.Println("ERROR: no path an olca-schema folder given")
		return
	}

	// parse the YAML files
	yamlDir := filepath.Join(os.Args[1], "yaml")
	types := collectTypes(yamlDir)
	typeMap := make(map[string]*TypeDef)
	for i := range types {
		typeDef := types[i]
		typeMap[typeDef.name()] = typeDef
	}

	var buff bytes.Buffer
	buff.WriteString(ProtoHeader)

	// write the message and enumeration types
	for _, typeDef := range types {
		switch typeDef.name() {
		case "Entity", "RootEntity", "CategorizedEntity":
			continue
		}

		// write a class definition
		class := typeDef.Class
		if class != nil {
			comment := formatComment(class.Doc, "")
			if comment != "" {
				buff.WriteString(comment)
			}
			buff.WriteString("message " + class.Name + " {\n\n")
			fields(class, &buff, typeMap, 1)
			buff.WriteString("}\n\n")
			continue
		}

		// write an enumeration
		enum := typeDef.Enum
		if enum != nil {
			comment := formatComment(enum.Doc, "")
			if comment != "" {
				buff.WriteString(comment)
			}
			buff.WriteString("enum " + enum.Name + " {\n\n")
			buff.WriteString("  // This default option was added automatically\n")
			buff.WriteString("  // and means that no values was set.\n")
			buff.WriteString("  " + undefinedOf(enum) + " = 0;\n\n")
			for i, item := range enum.Items {
				comment := formatComment(item.Doc, "  ")
				if comment != "" {
					buff.WriteString(comment)
				}
				buff.WriteString("  " + item.Name + " = " +
					strconv.Itoa(i+1) + ";\n\n")
			}
			buff.WriteString("}\n\n")
		}
	}

	// print to console or write to file
	if len(os.Args) < 3 {
		fmt.Println(buff.String())
	} else {
		outFile := os.Args[2]
		err := ioutil.WriteFile(outFile, buff.Bytes(), os.ModePerm)
		check(err, "failed to write to file", outFile)
	}
}

// Collects the type definitions from the YAML files in the given folder.
func collectTypes(yamlDir string) []*TypeDef {
	files, err := ioutil.ReadDir(yamlDir)
	check(err, "failed to read YAML files from", yamlDir)
	types := make([]*TypeDef, 0)
	for _, file := range files {
		name := file.Name()
		if !strings.HasSuffix(name, ".yaml") {
			continue
		}
		fmt.Println("Parse YAML file", name)
		path := filepath.Join(yamlDir, name)
		data, err := ioutil.ReadFile(path)
		check(err, "failed to read file", name)
		typeDef := &TypeDef{}
		err = yaml.Unmarshal(data, typeDef)
		check(err, "failed to parse file", name)
		fmt.Println("Parsed", typeDef)
		types = append(types, typeDef)
	}
	fmt.Println("Collected", len(types), "types")
	return types
}

// Writes the fields of the given class to the given buffer. This function
// climbs up the class hierarchy and inlines the fields of the corresponding
// super classes (as there is no extension mechanism in proto3).
func fields(class *ClassDef, buff *bytes.Buffer, types map[string]*TypeDef, offset int) int {
	count := offset

	// write fields of super classes recursively
	if class.SuperClass != "" {
		super := types[class.SuperClass]
		if super != nil && super.Class != nil {
			count = fields(super.Class, buff, types, offset)
		}
	}

	// @type field
	if class.Name == "Ref" || class.Name == "CategorizedEntity" {
		buff.WriteString("  // The type name of the respective entity.\n")
		buff.WriteString("  protolca.commons.EntityType entity_type = " + strconv.Itoa(count))
		buff.WriteString(" [json_name = \"@type\"];\n\n")
		count++
	}

	// ID field
	if class.Name == "RootEntity" {
		buff.WriteString("  // The reference ID (typically an UUID) of the entity.\n")
		buff.WriteString("  string id = " + strconv.Itoa(count))
		buff.WriteString(" [json_name = \"@id\"];\n\n")
		count++
	}

	// write fields
	for _, field := range class.Fields {

		// field comment
		comment := formatComment(field.Doc, "  ")
		if comment != "" {
			buff.WriteString(comment)
		}

		protoType := mapType(field.Type)
		protoField := toSnakeCase(field.Name)
		if protoType == "bytes" {
			buff.WriteString(BytesHint)
			protoField += "_bytes"
		}

		buff.WriteString("  " + protoType + " " + protoField +
			" = " + strconv.Itoa(count) + ";\n\n")
		count++
	}

	return count
}

// Maps the given olca-schema type to a corresponding proto3 type.
func mapType(schemaType string) string {
	switch schemaType {
	case "string", "double", "float":
		return schemaType
	case "dateTime", "date":
		return "string"
	case "int", "integer":
		return "int32"
	case "boolean":
		return "bool"
	case "GeoJSON":
		return "bytes"
	}

	if strings.HasPrefix(schemaType, "Ref[") {
		return "Ref"
	}
	if strings.HasPrefix(schemaType, "List[") {
		t := strings.TrimSuffix(
			strings.TrimPrefix(schemaType, "List["), "]")
		return "repeated " + mapType(t)
	}

	return schemaType
}

// Formats the given comment to have a line lenght of max. 80 characters.
func formatComment(comment string, indent string) string {
	if strings.TrimSpace(comment) == "" {
		return ""
	}

	// split words by whitespaces
	var words []string
	var word bytes.Buffer
	for _, char := range comment {
		if unicode.IsSpace(char) {
			if word.Len() > 0 {
				words = append(words, word.String())
			}
			word.Reset()
			continue
		}
		word.WriteRune(char)
	}
	if word.Len() > 0 {
		words = append(words, word.String())
	}
	if len(words) == 0 {
		return ""
	}

	// format the comment
	text := ""
	line := indent + "//"
	for _, word := range words {
		nextLine := line + " " + word
		if len(nextLine) < 80 {
			line = nextLine
		} else {
			text += line + "\n"
			line = indent + "// " + word
		}
	}
	if line != indent+"// " {
		text += line + "\n"
	}
	return text
}

// Generates the name of the `UNDEFINED` option for the given
// enumeration type. As this option has to have a unique name
// we include the name of the enumration into that name.
func undefinedOf(enum *EnumDef) string {
	var buff bytes.Buffer
	for _, char := range enum.Name {
		if unicode.IsUpper(char) {
			buff.WriteRune('_')
		}
		buff.WriteRune(char)
	}
	return "UNDEFINED" + strings.ToUpper(buff.String())
}

// Converts the given identifier from camelCase to snake_case.
func toSnakeCase(identifier string) string {
	var buff bytes.Buffer
	for i, char := range identifier {
		if i > 0 && unicode.IsUpper(char) {
			buff.WriteRune('_')
		}
		buff.WriteRune(unicode.ToLower(char))
	}
	return buff.String()
}

func check(err error, msg ...interface{}) {
	if err != nil {
		fmt.Print("ERROR: ")
		fmt.Println(msg...)
		panic(err)
	}
}
