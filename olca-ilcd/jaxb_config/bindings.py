
import csv
from string import Template

javaPrefix = ""


class SchemaBinding:
	
	def __init__(self, schema, package):
		self.schema = schema
		self.package = package
  
	def toXml(self, xmlFile):
		template = self.prepareTemplate()
		xml = template.substitute(schema = self.schema, 
					package = self.package)
		xmlFile.write(xml)
		
	def prepareTemplate(self):		
		template = Template("""
		<bindings
			schemaLocation="../schemas/$schema"
			node="/xs:schema">
			<schemaBindings>
				<package
					name="$package" />
			</schemaBindings>			
		</bindings>
		""")	
		return template	
	  
class TypeBinding: 
	
	def __init__(self, namespace, bindingType, xmlName, javaName):
		self.namespace = namespace
		self.bindingType = bindingType
		self.xmlName = xmlName
		self.javaName = javaPrefix + javaName

	def toXml(self, xmlFile):
		template = self.prepareTemplate()
		xml =  template.substitute(namespace=self.namespace, 
					xmlName=self.xmlName, bindingType=self.bindingType, 
					javaName=self.javaName)
		xmlFile.write(xml)
		
	def prepareTemplate(self):	
		template = Template("""
		<bindings
			scd='/type::$namespace:$xmlName'>
			<$bindingType
				name='$javaName'>
			</$bindingType>
		</bindings>
		""")
		return template

class FieldBinding:
	
	def __init__(self, schema, xmlTypeName, xmlFieldType, 
				xmlFieldName, javaFieldName):
		self.schema = schema
		self.xmlTypeName = xmlTypeName
		self.xmlFieldType = xmlFieldType
		self.xmlFieldName = xmlFieldName
		self.javaFieldName = javaFieldName

	def toXml(self, xmlFile):
		template = self.prepareTemplate()
		xml = template.substitute(
					schema = self.schema,
					xmlTypeName=self.xmlTypeName, 
					xmlFieldType=self.xmlFieldType, 
					xmlFieldName=self.xmlFieldName, 
					javaFieldName=self.javaFieldName)
		xmlFile.write(xml)

	def prepareTemplate(self):	
		template = Template("""
	   	<bindings
	   		schemaLocation="../schemas/$schema"
			node="//xs:complexType[@name='$xmlTypeName']//xs:$xmlFieldType[@name='$xmlFieldName']">
			<property
				name="$javaFieldName" />
		</bindings>
		""")
		return template

def createBindingFile(templateFile, outputFile, schemaBindingDef, typeBindingDef, fieldBindingDef):
	xmlTemplate =  open(templateFile, 'rb')
	xmlFile = open(outputFile, 'wb')
	for line in xmlTemplate.readlines():
		xmlFile.write(line)
		if line.strip() == "<!-- bindings -->":
			makeSchemaBindings(schemaBindingDef, xmlFile)
			makeTypeBindings(typeBindingDef, xmlFile)
			makeFieldBindings(fieldBindingDef, xmlFile)

def makeSchemaBindings(schemaBindingDef, xmlFile):
	rows = csv.reader(open(schemaBindingDef, 'rb'),
					delimiter=';', quotechar='\"')
	for row in rows:
		binding = SchemaBinding(row[0], row[1])
		binding.toXml(xmlFile)
		
def makeTypeBindings(typeBindingDef, xmlFile):
	rows = csv.reader(open(typeBindingDef, 'rb'),
					 delimiter=';', quotechar='\"')
	for row in rows:
		binding = TypeBinding(row[1], row[2], row[3], row[5])
		binding.toXml(xmlFile)

def makeFieldBindings(fieldBindingDef, xmlFile):
	rows = csv.reader(open(fieldBindingDef, 'rb'),
					   delimiter=';', quotechar='\"')
	for row in rows:
		binding = FieldBinding(row[0], row[1], row[2], row[3], row[4])
		binding.toXml(xmlFile)
	
createBindingFile("binding_template.xml", 
				"bindings.xml", 
				"schema_binding_def.csv", 
				"type_binding_def.csv", 
				"field_binding_def.csv")
