File expandedTemplate = new File( basedir, "target/jenkins-description.html" );
assert expandedTemplate.exists();
String content = expandedTemplate.getText("UTF-8");

assert content.contains('<a href="http://company/wiki/sampledev">Sample Developer&nbsp;<i>(developer)</i>');
assert content.contains('<!-- write your own html into property customHtml -->');
assert !content.contains('<p>All resistance is futile!</p>');
assert !content.contains('<a href="http://company/wiki/project1">My URL</a>');

File expandedTemplate2 = new File( basedir, "project1/target/jenkins-description.html" );
assert expandedTemplate2.exists();
String content2 = expandedTemplate2.getText("UTF-8");

assert content2.contains('<a href="http://company/wiki/sampledev">Sample Developer');
assert content2.contains('<p>All resistance is futile!</p>');
assert content2.contains('<a href="http://company/wiki/project1">My URL</a>');

return true;
