/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.jmeter.buildtools.jgit.dsl

import org.apache.jmeter.buildtools.release.GitConfig
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

fun TransportCommand<*, *>.setCredentials(repo: GitConfig) =
    setCredentialsProvider(
        repo.credentials.run {
            UsernamePasswordCredentialsProvider(
                username.get(),
                password.get()
            )
        }
    )

fun gitCloneRepository(action: org.eclipse.jgit.api.CloneCommand.() -> Unit) = Git.cloneRepository().apply { action() }.call()
fun gitInit(action: org.eclipse.jgit.api.InitCommand.() -> Unit) = Git.init().apply { action() }.call()
fun gitLsRemoteRepository(action: org.eclipse.jgit.api.LsRemoteCommand.() -> Unit) = Git.lsRemoteRepository().apply { action() }.call()

fun Git.add(action: org.eclipse.jgit.api.AddCommand.() -> Unit) = add().apply { action() }.call()
fun Git.apply(action: org.eclipse.jgit.api.ApplyCommand.() -> Unit) = apply().apply { action() }.call()
fun Git.archive(action: org.eclipse.jgit.api.ArchiveCommand.() -> Unit) = archive().apply { action() }.call()
fun Git.blame(action: org.eclipse.jgit.api.BlameCommand.() -> Unit) = blame().apply { action() }.call()
fun Git.branchCreate(action: org.eclipse.jgit.api.CreateBranchCommand.() -> Unit) = branchCreate().apply { action() }.call()
fun Git.branchDelete(action: org.eclipse.jgit.api.DeleteBranchCommand.() -> Unit) = branchDelete().apply { action() }.call()
fun Git.branchList(action: org.eclipse.jgit.api.ListBranchCommand.() -> Unit) = branchList().apply { action() }.call()
fun Git.branchRename(action: org.eclipse.jgit.api.RenameBranchCommand.() -> Unit) = branchRename().apply { action() }.call()
fun Git.checkout(action: org.eclipse.jgit.api.CheckoutCommand.() -> Unit) = checkout().apply { action() }.call()
fun Git.cherryPick(action: org.eclipse.jgit.api.CherryPickCommand.() -> Unit) = cherryPick().apply { action() }.call()
fun Git.clean(action: org.eclipse.jgit.api.CleanCommand.() -> Unit) = clean().apply { action() }.call()
fun Git.commit(action: org.eclipse.jgit.api.CommitCommand.() -> Unit) = commit().apply { action() }.call()
fun Git.describe(action: org.eclipse.jgit.api.DescribeCommand.() -> Unit) = describe().apply { action() }.call()
fun Git.diff(action: org.eclipse.jgit.api.DiffCommand.() -> Unit) = diff().apply { action() }.call()
fun Git.fetch(action: org.eclipse.jgit.api.FetchCommand.() -> Unit) = fetch().apply { action() }.call()
fun Git.gc(action: org.eclipse.jgit.api.GarbageCollectCommand.() -> Unit) = gc().apply { action() }.call()
fun Git.log(action: org.eclipse.jgit.api.LogCommand.() -> Unit) = log().apply { action() }.call()
fun Git.lsRemote(action: org.eclipse.jgit.api.LsRemoteCommand.() -> Unit) = lsRemote().apply { action() }.call()
fun Git.merge(action: org.eclipse.jgit.api.MergeCommand.() -> Unit) = merge().apply { action() }.call()
fun Git.nameRev(action: org.eclipse.jgit.api.NameRevCommand.() -> Unit) = nameRev().apply { action() }.call()
fun Git.notesAdd(action: org.eclipse.jgit.api.AddNoteCommand.() -> Unit) = notesAdd().apply { action() }.call()
fun Git.notesList(action: org.eclipse.jgit.api.ListNotesCommand.() -> Unit) = notesList().apply { action() }.call()
fun Git.notesRemove(action: org.eclipse.jgit.api.RemoveNoteCommand.() -> Unit) = notesRemove().apply { action() }.call()
fun Git.notesShow(action: org.eclipse.jgit.api.ShowNoteCommand.() -> Unit) = notesShow().apply { action() }.call()
fun Git.pull(action: org.eclipse.jgit.api.PullCommand.() -> Unit) = pull().apply { action() }.call()
fun Git.push(action: org.eclipse.jgit.api.PushCommand.() -> Unit) = push().apply { action() }.call()
fun Git.rebase(action: org.eclipse.jgit.api.RebaseCommand.() -> Unit) = rebase().apply { action() }.call()
fun Git.reflog(action: org.eclipse.jgit.api.ReflogCommand.() -> Unit) = reflog().apply { action() }.call()
fun Git.remoteAdd(action: org.eclipse.jgit.api.RemoteAddCommand.() -> Unit) = remoteAdd().apply { action() }.call()
fun Git.remoteList(action: org.eclipse.jgit.api.RemoteListCommand.() -> Unit) = remoteList().apply { action() }.call()
fun Git.remoteRemove(action: org.eclipse.jgit.api.RemoteRemoveCommand.() -> Unit) = remoteRemove().apply { action() }.call()
fun Git.remoteSetUrl(action: org.eclipse.jgit.api.RemoteSetUrlCommand.() -> Unit) = remoteSetUrl().apply { action() }.call()
fun Git.reset(action: org.eclipse.jgit.api.ResetCommand.() -> Unit) = reset().apply { action() }.call()
fun Git.revert(action: org.eclipse.jgit.api.RevertCommand.() -> Unit) = revert().apply { action() }.call()
fun Git.rm(action: org.eclipse.jgit.api.RmCommand.() -> Unit) = rm().apply { action() }.call()
fun Git.stashApply(action: org.eclipse.jgit.api.StashApplyCommand.() -> Unit) = stashApply().apply { action() }.call()
fun Git.stashCreate(action: org.eclipse.jgit.api.StashCreateCommand.() -> Unit) = stashCreate().apply { action() }.call()
fun Git.stashDrop(action: org.eclipse.jgit.api.StashDropCommand.() -> Unit) = stashDrop().apply { action() }.call()
fun Git.stashList(action: org.eclipse.jgit.api.StashListCommand.() -> Unit) = stashList().apply { action() }.call()
fun Git.status(action: org.eclipse.jgit.api.StatusCommand.() -> Unit) = status().apply { action() }.call()
fun Git.submoduleAdd(action: org.eclipse.jgit.api.SubmoduleAddCommand.() -> Unit) = submoduleAdd().apply { action() }.call()
fun Git.submoduleDeinit(action: org.eclipse.jgit.api.SubmoduleDeinitCommand.() -> Unit) = submoduleDeinit().apply { action() }.call()
fun Git.submoduleInit(action: org.eclipse.jgit.api.SubmoduleInitCommand.() -> Unit) = submoduleInit().apply { action() }.call()
fun Git.submoduleStatus(action: org.eclipse.jgit.api.SubmoduleStatusCommand.() -> Unit) = submoduleStatus().apply { action() }.call()
fun Git.submoduleSync(action: org.eclipse.jgit.api.SubmoduleSyncCommand.() -> Unit) = submoduleSync().apply { action() }.call()
fun Git.submoduleUpdate(action: org.eclipse.jgit.api.SubmoduleUpdateCommand.() -> Unit) = submoduleUpdate().apply { action() }.call()
fun Git.tag(action: org.eclipse.jgit.api.TagCommand.() -> Unit) = tag().apply { action() }.call()
fun Git.tagDelete(action: org.eclipse.jgit.api.DeleteTagCommand.() -> Unit) = tagDelete().apply { action() }.call()
fun Git.tagList(action: org.eclipse.jgit.api.ListTagCommand.() -> Unit) = tagList().apply { action() }.call()
