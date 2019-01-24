package dartagnan.parsers.cat.visitors;

import dartagnan.parsers.CatBaseVisitor;
import dartagnan.parsers.CatParser;
import dartagnan.parsers.CatVisitor;
import dartagnan.parsers.cat.utils.CatSyntaxException;
import dartagnan.wmm.filter.*;
import dartagnan.wmm.relation.Relation;

public class VisitorFilter extends CatBaseVisitor<FilterAbstract> implements CatVisitor<FilterAbstract> {

    private VisitorBase base;

    VisitorFilter(VisitorBase base){
        this.base = base;
    }

    @Override
    public FilterAbstract visitExpr(CatParser.ExprContext ctx) {
        return ctx.e.accept(this);
    }

    @Override
    public FilterAbstract visitExprIntersection(CatParser.ExprIntersectionContext ctx) {
        return new FilterIntersection(ctx.e1.accept(this), ctx.e2.accept(this));
    }

    @Override
    public FilterAbstract visitExprMinus(CatParser.ExprMinusContext ctx) {
        return new FilterMinus(ctx.e1.accept(this), ctx.e2.accept(this));
    }

    @Override
    public FilterAbstract visitExprUnion(CatParser.ExprUnionContext ctx) {
        return new FilterUnion(ctx.e1.accept(this), ctx.e2.accept(this));
    }

    @Override
    public FilterAbstract visitExprComplement(CatParser.ExprComplementContext ctx) {
        throw new RuntimeException("Filter complement is not implemented");
    }

    @Override
    public FilterAbstract visitExprRange(CatParser.ExprRangeContext ctx) {
        Relation relation = ctx.expression().accept(base.relationVisitor);
        if(relation != null){
            return new FilterRange(relation);
        }
        throw new CatSyntaxException(ctx.getText());
    }

    @Override
    public FilterAbstract visitExprBasic(CatParser.ExprBasicContext ctx) {
        FilterAbstract filter = base.wmm.getFilter(ctx.getText());
        if(filter == null){
            filter = new FilterBasic(ctx.getText());
            base.wmm.addFilter(filter);
        }
        return filter;
    }

    @Override
    public FilterAbstract visitExprCartesian(CatParser.ExprCartesianContext ctx) {
        throw new CatSyntaxException(ctx.getText());
    }

    @Override
    public FilterAbstract visitExprComposition(CatParser.ExprCompositionContext ctx) {
        throw new CatSyntaxException(ctx.getText());
    }

    @Override
    public FilterAbstract visitExprFencerel(CatParser.ExprFencerelContext ctx) {
        throw new CatSyntaxException(ctx.getText());
    }

    @Override
    public FilterAbstract visitExprIdentity(CatParser.ExprIdentityContext ctx) {
        throw new CatSyntaxException(ctx.getText());
    }

    @Override
    public FilterAbstract visitExprInverse(CatParser.ExprInverseContext ctx) {
        throw new CatSyntaxException(ctx.getText());
    }

    @Override
    public FilterAbstract visitExprOptional(CatParser.ExprOptionalContext ctx) {
        throw new CatSyntaxException(ctx.getText());
    }

    @Override
    public FilterAbstract visitExprTransitive(CatParser.ExprTransitiveContext ctx) {
        throw new CatSyntaxException(ctx.getText());
    }

    @Override
    public FilterAbstract visitExprTransRef(CatParser.ExprTransRefContext ctx) {
        throw new CatSyntaxException(ctx.getText());
    }
}