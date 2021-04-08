package com.dat3m.dartagnan.expression.op;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public enum COpBin {
    EQ, NEQ, GTE, LTE, GT, LT, UGTE, ULTE, UGT, ULT;

    @Override
    public String toString() {
        switch(this){
            case EQ:
                return "==";
            case NEQ:
                return "!=";
            case GTE:
            case UGTE:
                return ">=";
            case LTE:
            case ULTE:
                return "<=";
            case GT:
            case UGT:
                return ">";
            case LT:
            case ULT:
                return "<";
        }
        return super.toString();
    }

    public BoolExpr encode(Expr e1, Expr e2, Context ctx) {
        switch(this) {
            case EQ:
                return ctx.mkEq(e1, e2);
            case NEQ:
                return ctx.mkDistinct(e1, e2);
            case LT:
            	return ctx.mkBVSLT((BitVecExpr)e1, (BitVecExpr)e2);
            case ULT:
            	return ctx.mkBVULT((BitVecExpr)e1, (BitVecExpr)e2);
            case LTE:
                return ctx.mkBVSLE((BitVecExpr)e1, (BitVecExpr)e2);
            case ULTE:
                return ctx.mkBVULE((BitVecExpr)e1, (BitVecExpr)e2);
            case GT:
            	return ctx.mkBVSGT((BitVecExpr)e1, (BitVecExpr)e2);
            case UGT:
            	return ctx.mkBVUGT((BitVecExpr)e1, (BitVecExpr)e2);
            case GTE:
            	return ctx.mkBVSGE((BitVecExpr)e1, (BitVecExpr)e2);
            case UGTE:
            	return ctx.mkBVUGE((BitVecExpr)e1, (BitVecExpr)e2);
        }
        throw new UnsupportedOperationException("Encoding of not supported for COpBin " + this);
    }

    public boolean combine(int a, int b){
        switch(this){
            case EQ:
                return a == b;
            case NEQ:
                return a != b;
            case LT:
            case ULT:
                return a < b;
            case LTE:
            case ULTE:
                return a <= b;
            case GT:
            case UGT:
                return a > b;
            case GTE:
            case UGTE:
                return a >= b;
        }
        throw new UnsupportedOperationException("Illegal operator " + this + " in COpBin");
    }
}
