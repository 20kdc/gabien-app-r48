/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.mapinfos;

/**
 * Holds a few functions and their implementations.
 * Created on 02/06/17.
 */
public class MapInfoReparentUtil {
    public static int findChildrenLastOrder(int mapId, IRMLikeMapInfoBackend operators) {
        int order = operators.getOrderOfMap(mapId);

        // Recursively find the highest order, which ensures the whole tree will be moved.
        for (int key : operators.getHashKeys())
            if (operators.getHashBID(key).getInstVarBySymbol("@parent_id").fixnumVal == mapId)
                order = Math.max(order, findChildrenLastOrder(key, operators));
        return order;
    }

    // upper logic of the next function cut out.
    public static boolean wouldRelocatingInOrderFail(int orderA, int orderB, IRMLikeMapInfoBackendWPriv operators) {
        // This is one of those methods where not documenting it would be more of a pain
        //  than documenting it.
        int map = operators.getMapOfOrder(orderA);
        if (map == -1)
            return true;
        // When moving a tree about,
        //  the tree can (thankfully) be considered as one contiguous section.
        // However, the tree has to be known first.
        int lastOrder = findChildrenLastOrder(map, operators);
        // Essentially, what needs to happen here is:
        // 1. Detect if the place we want to go is inside where we are,
        //     or in exactly the same place,
        //     in which case just do nothing.
        if (orderB > orderA)
            if (orderB <= (lastOrder + 1))
                return true;
        return false;
    }

    // Relocates a map in the order, giving it the parent of the map directly above it (or -1).
    // This guarantees consistency - further edits can be performed afterwards.
    // Note that orderA is the map-in-order to target, and orderB is the place it needs to go.
    // Testing of this operation is best performed by hitting the buttons until something breaks.
    public static int relocateInOrder(int orderA, int orderB, IRMLikeMapInfoBackendWPriv operators) {
        // This is one of those methods where not documenting it would be more of a pain
        //  than documenting it.
        int map = operators.getMapOfOrder(orderA);
        if (map == -1)
            return orderA;
        // When moving a tree about,
        //  the tree can (thankfully) be considered as one contiguous section.
        // However, the tree has to be known first.
        int lastOrder = findChildrenLastOrder(map, operators);
        // Essentially, what needs to happen here is:
        // 1. Detect if the place we want to go is inside where we are,
        //     or in exactly the same place,
        //     in which case just do nothing.
        if (orderB > orderA)
            if (orderB <= (lastOrder + 1))
                return orderA; // Do nothing

        // ...

        // Uh, right, this NEEDS A REWRITE NOW for the new swapping primitive.
        // Swaps are all well and good in theory but algorithms have to be written to use them.
        int windowSize = (lastOrder - orderA) + 1;
        if (orderA <= orderB) {
            orderB -= windowSize;
            while (orderA < orderB) {
                // Ok, so, with diagrams!
                // ord
                // 012345
                // map
                // 012345
                //  A/ B
                //
                // windowSize: 2 (so orderA + windowSize = 3)
                //
                // 3<>2
                // 2<>1
                //
                // ord
                // 012345
                // map
                // 031245
                //   A/B
                // And so on.
                for (int i = windowSize; i > 0; i--)
                    operators.swapOrders(orderA + i, orderA + i - 1);
                orderA++;
            }
        } else {
            while (orderA > orderB) {
                // Ok, so, with diagrams (again)!
                // ord
                // 012345
                // map
                // 012345
                //  B A/
                // windowSize: 2 (so orderA + windowSize = 5, not that it matters)
                // 2<>3
                // 3<>4
                // ord
                // 012345
                // map
                // 013425

                for (int i = 0; i < windowSize; i++)
                    operators.swapOrders(orderA + i - 1, orderA + i);
                orderA--;
            }
        }

        // 3. Fix parenting. Orders start from 1.
        long newParent = 0;
        int newOrder = operators.getOrderOfMap(map);
        if (newOrder > 1) {
            int prevMap = operators.getMapOfOrder(newOrder - 1);
            if (prevMap != -1)
                newParent = prevMap;
        }
        operators.getHashBID(map).getInstVarBySymbol("@parent_id").fixnumVal = newParent;
        return newOrder;
    }

    public static void removeMapHelperSALT(int k, IRMLikeMapInfoBackendWPriv operators) {
        int glo = operators.getLastOrder();
        for (int i = operators.getOrderOfMap(k); i < glo; i++)
            operators.swapOrders(i, i + 1);
        // Last order is now the map to be removed...
    }
}