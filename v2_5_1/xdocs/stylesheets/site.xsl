<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<!-- Content Stylesheet for "jakarta-site2" Documentation -->
<!-- NOTE:  Changes here should also be reflected in "site.vsl" and vice
	  versa, so either Anakia or XSLT can be used for document generation.   -->


<!-- Outstanding Compatibility Issues (with Anakia-based stylesheets):

            *****  THIS STYLESHEET IS NOW VERY OUT OF DATE  *****
            
* Calculation of the hyperlink for navigation menu items (site.xsl prefixes
  with relative path unconditionally; needs conditional logic like the
  "projectanchor" macro).

* Handling of the <image> element to insert relative path prefixes

* Special table formatting of the <table>, <tr>, and <td> tags.  (I don't
  really like this as an approach for styling things, but it's needed
  for strict compatibility :-).

* Functional equivalent of "site_printable.vsl" not yet started.

-->


<!-- $Id$ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">


  <!-- Output method -->
  <xsl:output method="html"
				encoding="iso-8859-1"
				  indent="no"/>


  <!-- Defined parameters (overrideable) -->
  <xsl:param    name="relative-path" select="'.'"/>

  <!-- Defined variables (non-overrideable) -->
  <xsl:variable name="body-bg"       select="'#ffffff'"/>
  <xsl:variable name="body-fg"       select="'#000000'"/>
  <xsl:variable name="body-link"     select="'#525D76'"/>
  <xsl:variable name="banner-bg"     select="'#525D76'"/>
  <xsl:variable name="banner-fg"     select="'#ffffff'"/>
  <xsl:variable name="sub-banner-bg" select="'#828DA6'"/>
  <xsl:variable name="sub-banner-fg" select="'#ffffff'"/>
  <xsl:variable name="table-th-bg"   select="'#039acc'"/>
  <xsl:variable name="table-td-bg"   select="'#a0ddf0'"/>
  <xsl:variable name="source-color"  select="'#023264'"/>


  <!-- Process an entire document into an HTML page -->
  <xsl:template match="document">
	 <xsl:variable name="project"
					 select="document('project.xml')/project"/>

	 <html>
	 <head>
	 <title><xsl:value-of select="$project/title"/> - <xsl:value-of select="properties/title"/></title>
	 <xsl:for-each select="properties/author">
		<xsl:variable name="name">
		  <xsl:value-of select="."/>
		</xsl:variable>
		<xsl:variable name="email">
		  <xsl:value-of select="@email"/>
		</xsl:variable>
		<meta name="author" value="{$name}"/>
		<meta name="email" value="{$email}"/>
	 </xsl:for-each>
	 </head>

	 <body bgcolor="{$body-bg}" text="{$body-fg}" link="{$body-link}"
			 alink="{$body-link}" vlink="{$body-link}">

	 <table border="0" width="100%" cellspacing="4">

		<xsl:comment>PAGE HEADER</xsl:comment>
		<tr><td colspan="2">

		  <xsl:comment>JAKARTA LOGO</xsl:comment>
		  <a href="http://jakarta.apache.org/">
			 <img src="http://jakarta.apache.org/images/jakarta-logo.gif"
				 align="left" alt="The Jakarta Project" border="0"/>
		  </a>
		  <xsl:if test="$project/logo">
			 <xsl:variable name="alt">
				<xsl:value-of select="$project/logo"/>
			 </xsl:variable>
			 <xsl:variable name="home">
				<xsl:value-of select="$project/@href"/>
			 </xsl:variable>
			 <xsl:variable name="src">
				<xsl:value-of select="$project/logo/@href"/>
			 </xsl:variable>

			 <xsl:comment>PROJECT LOGO</xsl:comment>
			 <a href="{$home}">
				<img src="{$src}" align="right" alt="{$alt}" border="0"/>
			 </a>
		  </xsl:if>

		</td></tr>

		<xsl:comment>HEADER SEPARATOR</xsl:comment>
		<tr>
		  <td colspan="2">
			 <hr noshade="" size="1"/>
		  </td>
		</tr>

		<tr>

		  <xsl:comment>LEFT SIDE NAVIGATION</xsl:comment>
		  <td width="20%" valign="top" nowrap="true">
			 <xsl:apply-templates select="$project/body/menu"/>
		  </td>

		  <xsl:comment>RIGHT SIDE MAIN BODY</xsl:comment>
		  <td width="80%" valign="top" align="left">
			 <xsl:apply-templates select="body/section"/>
		  </td>

		</tr>

		<xsl:comment>FOOTER SEPARATOR</xsl:comment>
		<tr>
		  <td colspan="2">
			 <hr noshade="" size="1"/>
		  </td>
		</tr>

		<xsl:comment>PAGE FOOTER</xsl:comment>
		<tr><td colspan="2">
		  <div align="center"><font color="{$body-link}" size="-1"><em>
		  Copyright &#169; 1999-2001, Apache Software Foundation
		  </em></font></div>
		</td></tr>

	 </table>
	 </body>
	 </html>

  </xsl:template>


  <!-- Process a menu for the navigation bar -->
  <xsl:template match="menu">
	 <p><strong><xsl:value-of select="@name"/></strong></p>
	 <ul>
		<xsl:apply-templates select="item"/>
	 </ul>
  </xsl:template>


  <!-- Process a menu item for the navigation bar -->
  <xsl:template match="item">
	 <xsl:variable name="href">
		<xsl:value-of select="$relative-path"/><xsl:value-of select="@href"/>
	 </xsl:variable>
	 <li><a href="{$href}"><xsl:value-of select="@name"/></a></li>
  </xsl:template>


  <!-- Process a documentation section -->
  <xsl:template match="section">
	 <xsl:variable name="name">
		<xsl:value-of select="@anchor"/>
	 </xsl:variable>
	 <table border="0" cellspacing="0" cellpadding="2" width="100%">
		<!-- Section heading -->
		<tr><td bgcolor="{$banner-bg}">
			 <font color="{$banner-fg}" face="arial,helvetica.sanserif">
			 <a name="{$name}">
			 <strong><xsl:value-of select="@name"/></strong></a></font>
		</td></tr>
		<!-- Section body -->
		<tr><td><blockquote>
		  <xsl:apply-templates/>
		</blockquote></td></tr>
	 </table>
  </xsl:template>


  <!-- Process a documentation subsection -->
  <xsl:template match="subsection">
	 <xsl:variable name="anchor">
		<xsl:value-of select="@anchor"/>
	 </xsl:variable>
	 <table border="0" cellspacing="0" cellpadding="2" width="100%">
		<!-- Subsection heading -->
		<tr><td bgcolor="{$sub-banner-bg}">
			 <font color="{$sub-banner-fg}" face="arial,helvetica.sanserif">
			 <a name="{$anchor}">
			 <strong><xsl:value-of select="@name"/></strong></a></font>
		</td></tr>
		<!-- Subsection body -->
		<tr><td><blockquote>
		  <xsl:apply-templates/>
		</blockquote></td></tr>
	 </table>
  </xsl:template>


  <!-- Process a source code example -->
  <xsl:template match="source">
	 <div align="left">
		<table cellspacing="4" cellpadding="0" border="0">
		  <tr>
			 <td bgcolor="{$source-color}" width="1" height="1">
				<img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
			 </td>
			 <td bgcolor="{$source-color}" height="1">
				<img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
			 </td>
			 <td bgcolor="{$source-color}" width="1" height="1">
				<img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
			 </td>
		  </tr>
		  <tr>
			 <td bgcolor="{$source-color}" width="1">
				<img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
			 </td>
			 <td bgcolor="#ffffff" height="1"><pre>
				<xsl:value-of select="."/>
			 </pre></td>
			 <td bgcolor="{$source-color}" width="1">
				<img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
			 </td>
		  </tr>
		  <tr>
			 <td bgcolor="{$source-color}" width="1" height="1">
				<img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
			 </td>
			 <td bgcolor="{$source-color}" height="1">
				<img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
			 </td>
			 <td bgcolor="{$source-color}" width="1" height="1">
				<img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
			 </td>
		  </tr>
		</table>
	 </div>
  </xsl:template>


  <!-- Process everything else by just passing it through -->
  <xsl:template match="*|@*">
	 <xsl:copy>
		<xsl:apply-templates select="@*|*|text()"/>
	 </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
