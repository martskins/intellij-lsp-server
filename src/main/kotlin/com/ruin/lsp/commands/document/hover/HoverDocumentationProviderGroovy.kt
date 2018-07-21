package com.ruin.lsp.commands.document.hover

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.apache.commons.lang.StringEscapeUtils
import org.jetbrains.kotlin.idea.KotlinQuickDocumentationProvider
import org.jetbrains.plugins.groovy.GroovyRenameInputValidator
import org.jetbrains.plugins.groovy.lang.documentation.GroovyDocumentationProvider
import java.awt.Image

class HoverDocumentationProviderGroovy : AbstractDocumentationProvider() {
    // KotlinQuickDocumentationProvider is final.
    private val innerProvider = GroovyDocumentationProvider()

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return unescape(innerProvider.generateDoc(element, originalElement)?.split("\n")?.firstOrNull() ?: "")
    }


    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): MutableList<String>? {
        return innerProvider.getUrlFor(element, originalElement)
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return innerProvider.getQuickNavigateInfo(element, originalElement)
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, `object`: Any?, element: PsiElement?): PsiElement? {
        return innerProvider.getDocumentationElementForLookupItem(psiManager, `object`, element)
    }

    override fun getLocalImageForElement(element: PsiElement, imageSpec: String): Image? {
        return null
    }
    override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?): PsiElement? {
        return null
    }

    override fun getDocumentationElementForLink(psiManager: PsiManager, link: String, context: PsiElement?): PsiElement? {
        return innerProvider.getDocumentationElementForLink(psiManager, link, context)
    }


    private fun unescape(s: String): String {
        return s.replace("""<p>.*</p>""".toRegex(),  "") // remove docstring
            .replace("""<div class='definition'>.*</div>""".toRegex(),  "")
            .let { StringUtil.stripHtml(it, true) }
            .split("\n").first()
            .let(StringUtil::unescapeXml)
            .let(StringEscapeUtils::unescapeHtml) // converts HTML character entities
    }
}
