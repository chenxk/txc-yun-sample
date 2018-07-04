echo "./changeVersion.sh oldVersion newVersion"
echo $1
echo $2
find ./ -name pom.xml -o -name build.sh | grep -v target | xargs perl -pi -e "s|$1|$2|g"
