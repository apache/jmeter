################################################################################
# Apache JMeter Makefile written by Sean Dowd <sdowd@arcmail.com>.
#
# N.B. You will need GNU make to use this make file.
################################################################################

################################################################################
# Common definitions.  Edit these settings for your site.
#
# LOCATION     : where to write the classes and javadocs.
# JAVAC        : the java compiler
# JAVACOPTS    : the java compiler
# JAR          : the jar program
# CLASSDIR     : where to write class files
# DOCDIR       : where to put javadoc output
# JAVADOC      : the javadoc program
################################################################################

LOCATION     = .
JAVAC        = javac
JAVACOPTS    = -g -deprecation
JAR          = jar
CLASSDIR     = $(LOCATION)/classes
BINDIR       = ../bin
DOCDIR       = ../docs/api

# Javadoc 1.1
#JAVADOC      = javadoc
#JAVADOCOPTS  = -d $(DOCDIR) -version -package
# Javadoc 1.2
JAVADOC    = /usr/local/jdk1.2/bin/javadoc
JAVADOCOPT = -d $(DOCDIR) -version -private \
	-windowtitle "Apache JMeter API" \
	-splitindex -use \
	-link http://java.sun.com/products/jdk/1.2/docs/api

################################################################################
# You should not need to change anything below here.
################################################################################

# Packages is the list of packages inside our base package.
BASE = org.apache.jmeter
VERSION = 1.4.1-dev
PACKAGES = \
	$(BASE) \
	$(BASE).timers \
	$(BASE).visualizers \
	$(BASE).samplers \

DIRECTORIES   = $(subst .,/,$(PACKAGES))
BASEDIRECTORY = $(subst .,/,$(BASE))
ALLSOURCES    = $(foreach dir,$(DIRECTORIES),$(wildcard $(dir)/*.java))
BUILDDIR      = $(PWD)

jar: $(ALLSOURCES)
	test -d $(CLASSDIR) || mkdir $(CLASSDIR)
	$(JAVAC) $(JAVACOPTS) -d $(CLASSDIR) $(ALLSOURCES)
	cp -pr $(BASEDIRECTORY)/images $(CLASSDIR)/$(BASEDIRECTORY)
	cp -pr $(BASEDIRECTORY)/*.properties $(CLASSDIR)/$(BASEDIRECTORY)
	(cd $(CLASSDIR); $(JAR) cmf $(BUILDDIR)/MANIFEST ApacheJMeter.jar *)
	mv $(CLASSDIR)/ApacheJMeter.jar $(BINDIR)

javadoc:
	test -d $(DOCDIR) || mkdir $(DOCDIR)
	$(JAVADOC) $(JAVADOCOPT) $(PACKAGES)

all: jar javadoc

dist:   jar javadoc
	@echo Creating distribution...
	(cd ..; tar cvfz Apache_JMeter_$(VERSION).tar.gz .)

test:
	@echo ALLSOURCES: $(ALLSOURCES)
	@echo BUILDDIR: $(BUILDDIR)

clean:
	rm -rf $(CLASSDIR) $(DOCDIR) $(BINDIR)/ApacheJMeter.jar
