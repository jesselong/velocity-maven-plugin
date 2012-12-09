velocity-maven-plugin
=====================

Goals
-----

velocity - apply a template

apply-template - apply a template to a set of files

    <plugin>
        <groupId>net.rumati.maven.plugins</groupId>
        <artifactId>velocity-maven-plugin</artifactId>
        <executions>
            <execution>
                <phase>generate-resources</phase>
                <goals>
                    <goal>apply-template</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <template>src/main/template.vm</template>
            <fileSet>
                <directory>src/main/html</directory>
                <includes>
                    <include>**/*.html</include>
                </includes>
                <outputDirectory>${project.build.directory}/html</outputDirectory>
            </fileSet>
            <properties>
                <aProperty>aValue</aProperty>
            </properties>
        </configuration>
    </plugin>

The plugin defines the following parameters in the Velocity context (in addition to those defined in the properties
element) -

* content - content of input file
* inputFile - Name of input file, including path in FileSet directory
* inputRoot - Path to FileSet directory
* outputFile - Name of output file, including path in FileSet outputDirectory
* outputRoot - Path to FileSet outputDirectory
