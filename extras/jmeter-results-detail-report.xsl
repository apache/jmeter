<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" encoding="UTF-8" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />


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

<!-- Defined parameters (overrideable) -->
<xsl:param    name="showData" select="'N'"/>

<xsl:template match="testResults">
	<html>
		<head>
			<title>Load Test Results</title>
			<style type="text/css">
				body {
					font:normal 68% verdana,arial,helvetica;
					color:#000000;
				}
				table tr td, table tr th {
					font-size: 68%;
				}
				table.details tr th{
					font-weight: bold;
					text-align:left;
					background:#a6caf0;
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
				
	
				img
				{
				  border-width: 0px;
				}
				
				.expand_link
				{
				   position=absolute;
				   right: 0px;
				   width: 27px;
				   top: 1px;
				   height: 27px;
				}
				
				.page_details
				{
				   display: none;
				}
                                
                                .page_details_expanded
                                {
                                    display: block;
                                    display/* hide this definition from  IE5/6 */: table-row;
                                }


			</style>
			<script language="JavaScript"><![CDATA[
                           function expand(details_id)
			   {
			      
			      document.getElementById(details_id).className = "page_details_expanded";
			   }
			   
			   function collapse(details_id)
			   {
			      
			      document.getElementById(details_id).className = "page_details";
			   }
			   
			   function change(details_id)
			   {
			      if(document.getElementById(details_id+"_image").src.match("expand"))
			      {
			         document.getElementById(details_id+"_image").src = "collapse.png";
			         expand(details_id);
			      }
			      else
			      {
			         document.getElementById(details_id+"_image").src = "expand.png";
			         collapse(details_id);
			      } 
                           }
			]]></script>
		</head>
		<body>
		
			<xsl:call-template name="pageHeader" />
			
			<xsl:call-template name="summary" />
			<hr size="1" width="95%" align="left" />
			
			<xsl:call-template name="pagelist" />
			<hr size="1" width="95%" align="left" />
			
			<xsl:call-template name="detail" />

		</body>
	</html>
</xsl:template>

<xsl:template name="pageHeader">
	<h1>Load Test Results</h1>
	<table width="100%">
		<tr>
			<td align="left"></td>
			<td align="right">Designed for use with <a href="http://jmeter.apache.org/">JMeter</a> and <a href="http://ant.apache.org">Ant</a>.</td>
		</tr>
	</table>
	<hr size="1" />
</xsl:template>

<xsl:template name="summary">
	<h2>Summary</h2>
	<table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
		<tr valign="top">
			<th>Tests</th>
			<th>Failures</th>
			<th>Success Rate</th>
			<th>Average Time</th>
			<th>Min Time</th>
			<th>Max Time</th>
		</tr>
		<tr valign="top">
			<xsl:variable name="allCount" select="count(/testResults/sampleResult)" />
			<xsl:variable name="allFailureCount" select="count(/testResults/sampleResult[attribute::success='false'])" />
			<xsl:variable name="allSuccessCount" select="count(/testResults/sampleResult[attribute::success='true'])" />
			<xsl:variable name="allSuccessPercent" select="$allSuccessCount div $allCount" />
			<xsl:variable name="allTotalTime" select="sum(/testResults/sampleResult/@time)" />
			<xsl:variable name="allAverageTime" select="$allTotalTime div $allCount" />
			<xsl:variable name="allMinTime">
				<xsl:call-template name="min">
					<xsl:with-param name="nodes" select="/testResults/sampleResult/@time" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="allMaxTime">
				<xsl:call-template name="max">
					<xsl:with-param name="nodes" select="/testResults/sampleResult/@time" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="$allFailureCount &gt; 0">Failure</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<td>
				<xsl:value-of select="$allCount" />
			</td>
			<td>
				<xsl:value-of select="$allFailureCount" />
			</td>
			<td>
				<xsl:call-template name="display-percent">
					<xsl:with-param name="value" select="$allSuccessPercent" />
				</xsl:call-template>
			</td>
			<td>
				<xsl:call-template name="display-time">
					<xsl:with-param name="value" select="$allAverageTime" />
				</xsl:call-template>
			</td>
			<td>
				<xsl:call-template name="display-time">
					<xsl:with-param name="value" select="$allMinTime" />
				</xsl:call-template>
			</td>
			<td>
				<xsl:call-template name="display-time">
					<xsl:with-param name="value" select="$allMaxTime" />
				</xsl:call-template>
			</td>
		</tr>
	</table>
</xsl:template>

<xsl:template name="pagelist">
	<h2>Pages</h2>
	<table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
		<tr valign="top">
			<th>URL</th>
			<th>Tests</th>
			<th>Failures</th>
			<th>Success Rate</th>
			<th>Average Time</th>
			<th>Min Time</th>
			<th>Max Time</th>
			<th></th>
		</tr>
		<xsl:for-each select="/testResults/sampleResult[not(@label = preceding::*/@label)]">
			<xsl:variable name="label" select="@label" />
			<xsl:variable name="count" select="count(../sampleResult[@label = current()/@label])" />
			<xsl:variable name="failureCount" select="count(../sampleResult[@label = current()/@label][attribute::success='false'])" />
			<xsl:variable name="successCount" select="count(../sampleResult[@label = current()/@label][attribute::success='true'])" />
			<xsl:variable name="successPercent" select="$successCount div $count" />
			<xsl:variable name="totalTime" select="sum(../sampleResult[@label = current()/@label]/@time)" />
			<xsl:variable name="averageTime" select="$totalTime div $count" />
			<xsl:variable name="minTime">
				<xsl:call-template name="min">
					<xsl:with-param name="nodes" select="../sampleResult[@label = current()/@label]/@time" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="maxTime">
				<xsl:call-template name="max">
					<xsl:with-param name="nodes" select="../sampleResult[@label = current()/@label]/@time" />
				</xsl:call-template>
			</xsl:variable>
			<tr valign="top">
				<xsl:attribute name="class">
					<xsl:choose>
						<xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
					</xsl:choose>
				</xsl:attribute>
				<td>
				<xsl:if test="$failureCount > 0">
				  <a><xsl:attribute name="href">#<xsl:value-of select="$label" /></xsl:attribute>
				  <xsl:value-of select="$label" />
				  </a>
				</xsl:if>
				<xsl:if test="0 >= $failureCount">
				  <xsl:value-of select="$label" />
				</xsl:if>
				</td>
				<td>
					<xsl:value-of select="$count" />
				</td>
				<td>
					<xsl:value-of select="$failureCount" />
				</td>
				<td>
					<xsl:call-template name="display-percent">
						<xsl:with-param name="value" select="$successPercent" />
					</xsl:call-template>
				</td>
				<td>
					<xsl:call-template name="display-time">
						<xsl:with-param name="value" select="$averageTime" />
					</xsl:call-template>
				</td>
				<td>
					<xsl:call-template name="display-time">
						<xsl:with-param name="value" select="$minTime" />
					</xsl:call-template>
				</td>
				<td>
					<xsl:call-template name="display-time">
						<xsl:with-param name="value" select="$maxTime" />
					</xsl:call-template>
				</td>
				<td align="center">
				   <a href="">
				      <xsl:attribute name="href"><xsl:text/>javascript:change('page_details_<xsl:value-of select="position()" />')</xsl:attribute>
				      <img src="expand.png" alt="expand/collapse"><xsl:attribute name="id"><xsl:text/>page_details_<xsl:value-of select="position()" />_image</xsl:attribute></img>				      
				   </a>
				</td>
			</tr>
			
                        <tr class="page_details">
                           <xsl:attribute name="id"><xsl:text/>page_details_<xsl:value-of select="position()" /></xsl:attribute>
                           <td colspan="8" bgcolor="#FF0000">
                              <div align="center">
			         <b>Details for Page "<xsl:value-of select="$label" />"</b>
			         <table bordercolor="#000000" border="1"  cellpadding="0" cellspacing="0" width="95%">
			         <tr>
			            <th>Thread</th>
			            <th>Iteration</th>
			            <th>Time</th>
			            <th>Success</th>
			            <!--
			            itterations = position() - thread stuff
			            time="312"
			            timeStamp="1053622047640"
			            responseMessage="OK"
			            threadName="Thread Group-1"
			            dataType="text"
			            label="/CansysAandA/processAuthentication.do"
			            success="true"
			            -->
			         </tr>
			         		         
			         <xsl:for-each select="../sampleResult[@label = $label and @threadName != $label]">			         			            
			            <tr>
			               <td><xsl:value-of select="@threadName" /></td>
			               <td><xsl:value-of select="position()" /></td>
			               <td><xsl:value-of select="@time" />ms</td>
			               <td><xsl:value-of select="@success" /></td>
			            </tr>
			         </xsl:for-each>
			         
			         </table>
			      </div>
                           </td>
                        </tr>
			
		</xsl:for-each>
	</table>
</xsl:template>

<xsl:template name="detail">
	<xsl:variable name="allFailureCount" select="count(/testResults/sampleResult[attribute::success='false'])" />

	<xsl:if test="$allFailureCount > 0">
		<h2>Failure Detail</h2>

		<xsl:for-each select="/testResults/sampleResult[not(@label = preceding::*/@label)]">

			<xsl:variable name="failureCount" select="count(../sampleResult[@label = current()/@label][attribute::success='false'])" />

			<xsl:if test="$failureCount > 0">
				<h3><xsl:value-of select="@label" /><a><xsl:attribute name="name"><xsl:value-of select="@label" /></xsl:attribute></a></h3>

				<table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
				<tr valign="top">
					<th>Response</th>
					<th>Failure Message</th>
					<xsl:if test="$showData = 'y'">
					   <th>Response Data</th>
					</xsl:if>
				</tr>
			
				<xsl:for-each select="/testResults/sampleResult[@label = current()/@label][attribute::success='false']">
					<tr>
						<td><xsl:value-of select="@responseCode" /> - <xsl:value-of select="@responseMessage" /></td>
						<td><xsl:value-of select="assertionResult/@failureMessage" /></td>
						<xsl:if test="$showData = 'y'">
							<td><xsl:value-of select="./binary" /></td>
						</xsl:if>
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