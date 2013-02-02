File expandedTemplate;
String content;

expandedTemplate = new File( basedir, "target/groupId.txt" );
assert expandedTemplate.exists();
content = expandedTemplate.getText("UTF-8");
assert content.contains('test');

expandedTemplate = new File( basedir, "target/artifactId.txt" );
assert expandedTemplate.exists();
content = expandedTemplate.getText("UTF-8");
assert content.contains('company-parent-pom');

expandedTemplate = new File( basedir, "target/version.txt" );
assert expandedTemplate.exists();
content = expandedTemplate.getText("UTF-8");
assert content.contains('1.0-SNAPSHOT');

return true;
