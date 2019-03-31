# hello-maven-plugin
使用classgraph开发maven静态扫描spring mvc controller插件

```xml
<plugin>
      <groupId>io.docbot</groupId>
      <artifactId>hello-maven-plugin</artifactId>
      <version>1.3-SNAPSHOT</version>
      <executions>
          <execution>
              <goals>
                  <goal>print-spring-mvc</goal>
              </goals>
          </execution>
      </executions>
      <configuration>
          <showClassGraphLog>false</showClassGraphLog>
          <packages>
                  <package>io.docbot.rest</package>
          </packages>
          <outputPath>${project.build.directory}/generated
          </outputPath>
      </configuration>
</plugin>
```            
