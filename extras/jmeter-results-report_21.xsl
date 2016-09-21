<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

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

<!-- 
	Stylesheet for processing 2.1 output format test result files 
	To uses this directly in a browser, add the following to the JTL file as line 2:
	<? xml-stylesheet type="text/xsl" href="../extras/jmeter-results-report_21.xsl" ?>
	and you can then view the JTL in a browser
-->
	
<xsl:output method="html" indent="yes" encoding="UTF-8" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />

<xsl:param name="titleReport" select="'Load Test Results'"/>
<xsl:param name="dateReport" select="'date not defined'"/>

<xsl:template match="testResults">
	<html>
		<head>
			<title><xsl:value-of select="$titleReport" /></title>
			<style type="text/css">
				body {
					font:normal 68% verdana,arial,helvetica;
					color:#000000;
				}
				table tr td, table tr th {
					font-size: 68%;
				}
				table.details tr th{
				    color: #ffffff;
					font-weight: bold;
					text-align:center;
					background:#2674a6;
					white-space: nowrap;
				}
				table.details tr td{
					background:#eeeee0;
					white-space: nowrap;
				}
				h1 {
					margin: 0px 0px 5px; font: 165% verdana,arial,helvetica
				}
				h2 {
					margin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica
				}
				h3 {
					margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica
				}
				.Failure {
					font-weight:bold; color:red;
				}
			</style>
		</head>
		<body>
		
			<xsl:call-template name="pageHeader" />
			
			<xsl:call-template name="summary" />
			<hr size="1" width="95%" align="center" />
			
			<xsl:call-template name="pagelist" />
			<hr size="1" width="95%" align="center" />
			
			<xsl:call-template name="detail" />

		</body>
	</html>
</xsl:template>

<xsl:template name="pageHeader">
	<h1><xsl:value-of select="$titleReport" /></h1>
	<table width="100%">
		<tr>
			<td align="left">Date report: <xsl:value-of select="$dateReport" /></td>
			<td align="right">Designed for use with <a href="http://jmeter.apache.org/">JMeter</a> and <a href="http://ant.apache.org">Ant</a>.</td>
		</tr>
	</table>
	<hr size="1" />
</xsl:template>

<xsl:template name="summary">
	<h2>Summary</h2>
	<table align="center" class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
		<tr valign="top">
			<th># Samples</th>
			<th>Failures</th>
			<th>Success Rate</th>
			<th>Average Time</th>
			<th>Min Time</th>
			<th>Max Time</th>
		</tr>
		<tr valign="top">
			<xsl:variable name="allCount" select="count(/testResults/*)" />
			<xsl:variable name="allFailureCount" select="count(/testResults/*[attribute::s='false'])" />
			<xsl:variable name="allSuccessCount" select="count(/testResults/*[attribute::s='true'])" />
			<xsl:variable name="allSuccessPercent" select="$allSuccessCount div $allCount" />
			<xsl:variable name="allTotalTime" select="sum(/testResults/*/@t)" />
			<xsl:variable name="allAverageTime" select="$allTotalTime div $allCount" />
			<xsl:variable name="allMinTime">
				<xsl:call-template name="min">
					<xsl:with-param name="nodes" select="/testResults/*/@t" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="allMaxTime">
				<xsl:call-template name="max">
					<xsl:with-param name="nodes" select="/testResults/*/@t" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="$allFailureCount &gt; 0">Failure</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<td align="center">
				<xsl:value-of select="$allCount" />
			</td>
			<td align="center">
				<xsl:value-of select="$allFailureCount" />
			</td>
			<td align="right">
				<xsl:call-template name="display-percent">
					<xsl:with-param name="value" select="$allSuccessPercent" />
				</xsl:call-template>
			</td>
			<td align="right">
				<xsl:call-template name="display-time">
					<xsl:with-param name="value" select="$allAverageTime" />
				</xsl:call-template>
			</td>
			<td align="right">
				<xsl:call-template name="display-time">
					<xsl:with-param name="value" select="$allMinTime" />
				</xsl:call-template>
			</td>
			<td align="right">
				<xsl:call-template name="display-time">
					<xsl:with-param name="value" select="$allMaxTime" />
				</xsl:call-template>
			</td>
		</tr>
	</table>
</xsl:template>
<xsl:template name="pagelist">
	<h2>Pages</h2>
	<table align="center" class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
		<tr valign="top">
			<th>URL</th>
			<th># Samples</th>
			<th>Failures</th>
			<th>Success Rate</th>
			<th>Average Time</th>
			<th>Min Time</th>
			<th>Max Time</th>
		</tr>
		<xsl:for-each select="/testResults/*[not(@lb = preceding::*/@lb)]">
			<xsl:variable name="label" select="@lb" />
			<xsl:variable name="count" select="count(../*[@lb = current()/@lb])" />
			<xsl:variable name="failureCount" select="count(../*[@lb = current()/@lb][attribute::s='false'])" />
			<xsl:variable name="successCount" select="count(../*[@lb = current()/@lb][attribute::s='true'])" />
			<xsl:variable name="successPercent" select="$successCount div $count" />
			<xsl:variable name="totalTime" select="sum(../*[@lb = current()/@lb]/@t)" />
			<xsl:variable name="averageTime" select="$totalTime div $count" />
			<xsl:variable name="minTime">
				<xsl:call-template name="min">
					<xsl:with-param name="nodes" select="../*[@lb = current()/@lb]/@t" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="maxTime">
				<xsl:call-template name="max">
					<xsl:with-param name="nodes" select="../*[@lb = current()/@lb]/@t" />
				</xsl:call-template>
			</xsl:variable>
			<tr valign="top">
				<xsl:attribute name="class">
					<xsl:choose>
						<xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
					</xsl:choose>
				</xsl:attribute>
				<td>
					<xsl:value-of select="$label" />
				</td>
				<td align="center">
					<xsl:value-of select="$count" />
				</td>
				<td align="center">
					<xsl:value-of select="$failureCount" />
				</td>
				<td align="right">
					<xsl:call-template name="display-percent">
						<xsl:with-param name="value" select="$successPercent" />
					</xsl:call-template>
				</td>
				<td align="right">
					<xsl:call-template name="display-time">
						<xsl:with-param name="value" select="$averageTime" />
					</xsl:call-template>
				</td>
				<td align="right">
					<xsl:call-template name="display-time">
						<xsl:with-param name="value" select="$minTime" />
					</xsl:call-template>
				</td>
				<td align="right">
					<xsl:call-template name="display-time">
						<xsl:with-param name="value" select="$maxTime" />
					</xsl:call-template>
				</td>
			</tr>
		</xsl:for-each>
	</table>
</xsl:template>

<xsl:template name="detail">
	<xsl:variable name="allFailureCount" select="count(/testResults/*[attribute::s='false'])" />

	<xsl:if test="$allFailureCount > 0">
		<h2>Failure Detail</h2>

		<xsl:for-each select="/testResults/*[not(@lb = preceding::*/@lb)]">

			<xsl:variable name="failureCount" select="count(../*[@lb = current()/@lb][attribute::s='false'])" />

			<xsl:if test="$failureCount > 0">
				<h3><xsl:value-of select="@lb" /></h3>

				<table align="center" class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
				<tr valign="top">
					<th>Response</th>
					<th>Failure Message</th>
				</tr>
			
				<xsl:for-each select="/testResults/*[@lb = current()/@lb][attribute::s='false']">
					<tr>
						<td><xsl:value-of select="@rc | @rs" /> - <xsl:value-of select="@rm" /></td>
						<td><xsl:value-of select="assertionResult/failureMessage" /></td>
					</tr>
				</xsl:for-each>
				
				</table>
			</xsl:if>

		</xsl:for-each>
	</xsl:if>
</xsl:template>

<xsl:template name="min">
	<xsl:param name="nodes" select="/.." />
	<xsl:choose>
		<xsl:when test="not($nodes)">NaN</xsl:when>
		<xsl:otherwise>
			<xsl:for-each select="$nodes">
				<xsl:sort data-type="number" />
				<xsl:if test="position() = 1">
					<xsl:value-of select="number(.)" />
				</xsl:if>
			</xsl:for-each>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="max">
	<xsl:param name="nodes" select="/.." />
	<xsl:choose>
		<xsl:when test="not($nodes)">NaN</xsl:when>
		<xsl:otherwise>
			<xsl:for-each select="$nodes">
				<xsl:sort data-type="number" order="descending" />
				<xsl:if test="position() = 1">
					<xsl:value-of select="number(.)" />
				</xsl:if>
			</xsl:for-each>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="display-percent">
	<xsl:param name="value" />
	<xsl:value-of select="format-number($value,'0.00%')" />
</xsl:template>

<xsl:template name="display-time">
	<xsl:param name="value" />
	<xsl:value-of select="format-number($value,'0 ms')" />
</xsl:template>
	
</xsl:stylesheet>