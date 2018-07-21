package com.ruin.lsp.commands.document.rename

import com.intellij.ide.DataManager
import com.intellij.lang.properties.psi.impl.PropertyImpl
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiResourceVariable
import com.intellij.psi.PsiTypeVariable
import com.intellij.psi.impl.PsiVariableEx
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.intellij.refactoring.RefactoringActionHandlerFactory
import com.intellij.refactoring.RefactoringFactory
import com.intellij.refactoring.actions.RenameElementAction
import com.intellij.refactoring.rename.PsiElementRenameHandler
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.refactoring.rename.RenameUtil
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.refactoring.rename.naming.AutomaticVariableRenamerFactory
import com.intellij.refactoring.rename.naming.PsiNamedElementAutomaticRenamer
import com.intellij.refactoring.util.MoveRenameUsageInfo
import com.intellij.usageView.UsageInfo
import com.ruin.lsp.commands.DocumentCommand
import com.ruin.lsp.commands.ExecutionContext
import com.ruin.lsp.model.MyLanguageServer
import com.ruin.lsp.model.MyWorkspaceService
import com.ruin.lsp.model.WorkspaceManager
import com.ruin.lsp.util.differenceFromAction
import com.ruin.lsp.util.getDocument
import com.ruin.lsp.util.toOffset
import com.ruin.lsp.util.withEditor
import org.eclipse.lsp4j.*
import org.jetbrains.kotlin.asJava.namedUnwrappedElement
import org.jetbrains.kotlin.idea.core.moveCaret
import org.jetbrains.kotlin.idea.refactoring.canRefactor
import org.jetbrains.kotlin.idea.refactoring.rename.findElementForRename
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.plugins.groovy.lang.psi.impl.auxiliary.GrLightIdentifier
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringBundle
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringSupportProvider
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringUtil
import org.jetbrains.plugins.groovy.refactoring.rename.GroovyAutomaticOverloadsRenamerFactory

private val LOG = Logger.getInstance(WorkspaceManager::class.java)

class RenameCommand(val position: Position, val identifier: TextDocumentIdentifier, val newName: String) : DocumentCommand<WorkspaceEdit>{
    override fun execute(ctx: ExecutionContext): WorkspaceEdit {
        val edits = differenceFromAction(ctx.file) { editor, copy ->
            val doc = getDocument(ctx.file)
            val offset = position.toOffset(doc!!)

            val elem = copy.findElementForRename<PsiNamedElement>(offset)
            val usages = arrayOf<UsageInfo>()
            ApplicationManager.getApplication().runWriteAction {
                RefactoringFactory.getInstance(editor.project).createRename(elem!!, newName).doRefactoring(usages)
            }
        }

        val vtx = VersionedTextDocumentIdentifier()
        vtx.uri = identifier.uri
        return WorkspaceEdit(arrayListOf(TextDocumentEdit(vtx, edits)).toList())
    }
}
