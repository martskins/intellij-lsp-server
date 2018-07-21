package com.ruin.lsp.commands.document.rename

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.*
import com.intellij.refactoring.RefactoringFactory
import com.ruin.lsp.commands.DocumentCommand
import com.ruin.lsp.commands.ExecutionContext
import com.ruin.lsp.util.differenceFromAction
import com.ruin.lsp.util.getDocument
import com.ruin.lsp.util.toOffset
import org.eclipse.lsp4j.*
import org.jetbrains.kotlin.idea.refactoring.rename.findElementForRename

class RenameCommand(val position: Position, private val identifier: TextDocumentIdentifier, private val newName: String) : DocumentCommand<WorkspaceEdit>{
    override fun execute(ctx: ExecutionContext): WorkspaceEdit {
        val edits = differenceFromAction(ctx.file) { editor, copy ->
            val doc = getDocument(copy)
            val offset = position.toOffset(doc!!)

            val elem = copy.findElementForRename<PsiNamedElement>(offset)
            if (elem == null) {
                ctx.client!!.showMessage(MessageParams(MessageType.Error, "Could not find element for rename"))
                return@differenceFromAction
            }

            ApplicationManager.getApplication().runWriteAction {
                val factory = RefactoringFactory.getInstance(editor.project).createRename(elem, newName, false, true)
                val usages = factory.findUsages()
                factory.doRefactoring(usages)
                ctx.client!!.showMessage(MessageParams(MessageType.Log, "Renamed ${usages.size + 1} occurrences"))
            }
        }

        val vtx = VersionedTextDocumentIdentifier()
        vtx.uri = identifier.uri
        return WorkspaceEdit(arrayListOf(TextDocumentEdit(vtx, edits)).toList())
    }
}
