File expandedTemplate = new File( basedir, "target/jenkins-description.html" );
assert expandedTemplate.exists();
String content = expandedTemplate.getText("UTF-8");

assert content.contains('<a href="http://company/wiki/sampledev">Sample Developer&nbsp;<i>(developer)</i>');

return true;
