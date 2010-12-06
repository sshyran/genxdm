/**
 * Portions copyright (c) 1998-1999, James Clark : see copyingjc.txt for
 * license details
 * Portions copyright (c) 2002, Bill Lindsey : see copying.txt for license
 * details
 * 
 * Portions copyright (c) 2009-2010 TIBCO Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.genxdm.processor.xpath.v10.expressions;

import org.genxdm.base.Model;
import org.genxdm.xpath.v10.expressions.ConvertibleNodeSetExpr;
import org.genxdm.xpath.v10.expressions.ExprContextDynamic;
import org.genxdm.xpath.v10.expressions.ExprException;
import org.genxdm.xpath.v10.expressions.NodeSetExpr;
import org.genxdm.xpath.v10.iterators.NodeIterator;
import org.genxdm.xpath.v10.iterators.UnionNodeIterator;

final class UnionExpr
    extends ConvertibleNodeSetExpr
{
	private final NodeSetExpr expr1;
	private final NodeSetExpr expr2;

	UnionExpr(final NodeSetExpr expr1, final NodeSetExpr expr2)
	{
		super();
		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	public <N> NodeIterator<N> nodeIterator(Model<N> model, final N node, final ExprContextDynamic<N> dynEnv) throws ExprException
	{
		return new UnionNodeIterator<N>(expr1.nodeIterator(model, node, dynEnv),
				expr2.nodeIterator(model, node, dynEnv), model);
	}
}
