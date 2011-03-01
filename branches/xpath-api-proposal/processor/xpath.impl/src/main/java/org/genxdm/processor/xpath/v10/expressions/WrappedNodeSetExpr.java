package org.genxdm.processor.xpath.v10.expressions;

import org.genxdm.Model;
import org.genxdm.processor.xpath.v10.variants.NodeSetVariant;
import org.genxdm.xpath.v10.Converter;
import org.genxdm.xpath.v10.ExprContextDynamic;
import org.genxdm.xpath.v10.ExprContextStatic;
import org.genxdm.xpath.v10.ExprException;
import org.genxdm.xpath.v10.NodeIterator;
import org.genxdm.xpath.v10.NodeSetExpr;
import org.genxdm.xpath.v10.Variant;
import org.genxdm.xpath.v10.extend.IConvertibleBooleanExpr;
import org.genxdm.xpath.v10.extend.IConvertibleStringExpr;
import org.genxdm.xpath.v10.extend.IConvertibleVariantExpr;

public class WrappedNodeSetExpr extends ConvertibleNodeSetExpr {

	public WrappedNodeSetExpr(NodeSetExpr expr, int optimizeFlags) {
		m_nodeSetExpr = expr;
		m_optimizeFlags = optimizeFlags;
	}
	
	@Override
	public <N> NodeIterator<N> nodeIterator(Model<N> model, N contextNode,
			ExprContextDynamic<N> dynEnv) throws ExprException {
		return m_nodeSetExpr.nodeIterator(model, contextNode, dynEnv);
	}

	@Override
	public IConvertibleStringExpr makeStringExpr(ExprContextStatic statEnv) {
		return new ConvertibleStringExpr() {
			public <N> String stringFunction(Model<N> model, final N node, final ExprContextDynamic<N> dynEnv)
			throws ExprException {
				return Converter.toString(m_nodeSetExpr.nodeIterator(model, node, dynEnv), model);
			}
		};
	}

	@Override
	public IConvertibleBooleanExpr makeBooleanExpr(ExprContextStatic statEnv) {
		return new ConvertibleBooleanExpr( ) {
			public <N> boolean booleanFunction(Model<N> model, final N node, final ExprContextDynamic<N> dynEnv) throws ExprException {
				return Converter.toBoolean(m_nodeSetExpr.nodeIterator(model, node, dynEnv));
			}
		};
	}

	@Override
	public IConvertibleVariantExpr makeVariantExpr(ExprContextStatic statEnv) {
		return new ConvertibleVariantExpr() {
			public <N> Variant<N> evaluateAsVariant(Model<N> model, final N contextNode, final ExprContextDynamic<N> dynEnv)
			throws ExprException {
				return new NodeSetVariant<N>(m_nodeSetExpr.nodeIterator(model, contextNode, dynEnv), model);
			}
		};
	}

	@Override
	public int getOptimizeFlags() {
		return m_optimizeFlags;
	}

	private NodeSetExpr m_nodeSetExpr;

	private int m_optimizeFlags;
}