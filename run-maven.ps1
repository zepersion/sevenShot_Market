$env:JAVA_HOME = 'C:\Program Files\Java\jdk-23'
Set-Location 'E:\SevenShot Market\SevenShot_Market'
& .\mvnw.cmd dependency:resolve 2>&1
