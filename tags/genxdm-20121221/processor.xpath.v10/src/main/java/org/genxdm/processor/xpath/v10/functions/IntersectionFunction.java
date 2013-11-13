/*
 * Portions copyright (c) 1998-1999, James Clark : see copyingjc.txt for
 * license details
 * Portions copyright (c) 2002, Bill Lindsey : see copying.txt for license
 * details
 * 
 * Portions copyright (c) 2009-2011 TIBCO Software Inc.
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
package org.genxdm.processor.xpath.v10.functions;

import org.genxdm.Model;
import org.genxdm.nodes.Traverser;
import org.genxdm.nodes.TraversingInformer;
import org.genxdm.processor.xpath.v10.expressions.ConvertibleExprImpl;
import org.genxdm.processor.xpath.v10.expressions.ConvertibleNodeSetExprImpl;
import org.genxdm.processor.xpath.v10.iterators.IntersectionNodeIterator;
import org.genxdm.processor.xpath.v10.iterators.IntersectionTraverser;
import org.genxdm.xpath.v10.TraverserDynamicContext;
import org.genxdm.xpath.v10.NodeDynamicContext;
import org.genxdm.xpath.v10.StaticContext;
import org.genxdm.xpath.v10.ExprParseException;
import org.genxdm.xpath.v10.NodeIterator;
import org.genxdm.xpath.v10.NodeSetExpr;
import org.genxdm.xpath.v10.extend.ConvertibleExpr;

final class IntersectionFunction 
    extends Function2
{
	public IntersectionFunction()
	{
		super();
	}

	ConvertibleExprImpl makeCallExpr(final ConvertibleExpr e1, final ConvertibleExpr e2, final StaticContext statEnv) throws ExprParseException
	{
		final NodeSetExpr nse1 = e1.makeNodeSetExpr(statEnv);
		final NodeSetExpr nse2 = e2.makeNodeSetExpr(statEnv);

		return new ConvertibleNodeSetExprImpl()
		{
            @Override
			public <N> NodeIterator<N> nodeIterator(Model<N> model, final N node, final NodeDynamicContext<N> dynEnv) {
				return new IntersectionNodeIterator<N>(nse1.nodeIterator(model, node, dynEnv),
						nse2.nodeIterator(model, node, dynEnv), model);
			}

            @Override
            public Traverser traverseNodes(TraversingInformer contextNode, TraverserDynamicContext dynEnv) {
                return new IntersectionTraverser(nse1.traverseNodes(contextNode, dynEnv),
                        nse2.traverseNodes(contextNode, dynEnv));
            }
		};
	}
}