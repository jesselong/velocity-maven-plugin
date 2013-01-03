velocity-maven-plugin
=====================

Goals
-----

velocity - execute a template

    <plugin>
        <groupId>net.rumati.maven.plugins</groupId>
        <artifactId>velocity-maven-plugin</artifactId>
        <executions>
            <execution>
                <phase>generate-resources</phase>
                <goals>
                    <goal>velocity</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <template>src/main/velocity/report.vm</template>
            <outputFile>target/report.html</outputFile>
            <properties>
                <aProperty>aValue</aProperty>
            </properties>
            <encoding>UTF8</encoding>
        </configuration>
    </plugin>

The plugin defines the following parameters in the Velocity context (in addition to those defined in the properties
element) -

* project.* - Maven project properties
* system.* - Properties in System.getProperties
* env.* - Properties in System.getenv

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
            <templateDirectory>src/main/velocity</templateDirectory>
            <template>src/main/velocity/template.vm</template>
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
            <encoding>UTF8</encoding>
        </configuration>
    </plugin>

The plugin defines the following parameters in the Velocity context (in addition to those defined in the properties
element) -

* content - content of input file
* inputPath - Path of input file
* inputFile - Name of input file, including path in FileSet directory
* outputPath - Path of output file
* outputFile - Name of output file, including path in FileSet outputDirectory
* relativePath - Relative path to FileSet outputDirectory
* project.* - Maven project properties
* system.* - Properties in System.getProperties
* env.* - Properties in System.getenv
