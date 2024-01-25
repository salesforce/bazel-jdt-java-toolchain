/*******************************************************************************
 * Copyright (c) 2019, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

public class TextBlock extends StringLiteral {

	public int endLineNumber;

	public TextBlock(char[] token, int start, int end, int lineNumber, int endLineNumber) {
		super(token, start,end, lineNumber);
		this.endLineNumber= endLineNumber - 1; // line number is 1 based
	}

}
