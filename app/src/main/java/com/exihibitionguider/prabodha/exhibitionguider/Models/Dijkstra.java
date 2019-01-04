package com.exihibitionguider.prabodha.exhibitionguider.Models;

import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Dijkstra {

    private ArrayList<Vertex> vertices = new ArrayList<>();

    public void computePaths(int sourceID)
    {
        Vertex source = vertices.get(sourceID-1);
        source.minDistance = 0.;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Vertex u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Edge e : u.adjacencies)
            {
                Vertex v = e.target;
                double weight = e.weight;
                double distanceThroughU = u.minDistance + weight;
                if (distanceThroughU < v.minDistance) {
                    vertexQueue.remove(v);

                    v.minDistance = distanceThroughU ;
                    v.previous = u;
                    vertexQueue.add(v);
                }
            }
        }
    }

    public List<Vertex> getShortestPathTo(int targetID)
    {
        Vertex target = vertices.get(targetID-1);
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);

        Collections.reverse(path);
        return path;
    }

    public ArrayList<Vertex> getVertices() {
        return vertices;
    }

    public void createGraph(){
        Vertex A = new Vertex("1");
        Vertex B = new Vertex("2");
        Vertex C = new Vertex("3");
        Vertex D = new Vertex("4");
        Vertex E = new Vertex("5");
        Vertex F = new Vertex("6");
        Vertex G = new Vertex("7");
        Vertex H = new Vertex("8");
        Vertex I = new Vertex("9");
        Vertex J = new Vertex("10");
        Vertex K = new Vertex("11");
        Vertex L = new Vertex("12");
        Vertex M = new Vertex("13");
        Vertex N = new Vertex("14");
        Vertex O = new Vertex("15");
        Vertex P = new Vertex("16");
        Vertex Q = new Vertex("17");
        Vertex R = new Vertex("18");
        Vertex S = new Vertex("19");

        // set the edges and weight
        A.adjacencies = new Edge[]{ new Edge(B, 5), new Edge(Q, 10) };
        B.adjacencies = new Edge[]{ new Edge(A, 5), new Edge(C, 10) };
        C.adjacencies = new Edge[]{ new Edge(B, 10), new Edge(D, 15), new Edge(S, 16) };
        D.adjacencies = new Edge[]{ new Edge(C, 15), new Edge(E, 10), new Edge(S, 5)};
        E.adjacencies = new Edge[]{ new Edge(D, 10), new Edge(F, 5) };
        F.adjacencies = new Edge[]{ new Edge(E, 5), new Edge(G, 10) };
        G.adjacencies = new Edge[]{ new Edge(F, 10), new Edge(H, 5) };
        H.adjacencies = new Edge[]{ new Edge(G, 5), new Edge(I, 5) };
        I.adjacencies = new Edge[]{ new Edge(H, 5), new Edge(J, 5) };
        J.adjacencies = new Edge[]{ new Edge(I, 5), new Edge(K, 5) };
        K.adjacencies = new Edge[]{ new Edge(J, 5), new Edge(L, 5), new Edge(R, 5) };
        L.adjacencies = new Edge[]{ new Edge(K, 5), new Edge(M, 5), new Edge(R, 5) };
        M.adjacencies = new Edge[]{ new Edge(L, 5), new Edge(N, 5) };
        N.adjacencies = new Edge[]{ new Edge(M, 5), new Edge(O, 5) };
        O.adjacencies = new Edge[]{ new Edge(N, 5), new Edge(P, 5) };
        P.adjacencies = new Edge[]{ new Edge(O, 5), new Edge(Q, 10) };
        Q.adjacencies = new Edge[]{ new Edge(P, 10), new Edge(A, 10) };
        R.adjacencies = new Edge[]{ new Edge(K, 5), new Edge(S, 10), new Edge(L, 5) };
        S.adjacencies = new Edge[]{ new Edge(R, 10), new Edge(C, 16), new Edge(D, 5) };

        vertices.add(A); vertices.add(B); vertices.add(C); vertices.add(D);
        vertices.add(E); vertices.add(F); vertices.add(G); vertices.add(H);
        vertices.add(I); vertices.add(J); vertices.add(K); vertices.add(L);
        vertices.add(M); vertices.add(N); vertices.add(O); vertices.add(P);
        vertices.add(Q); vertices.add(R); vertices.add(S);



    }



    public Vertex getVertex(int id){
        return vertices.get(id-1);
    }


//    public static void main(String[] args)
//    {
//        // mark all the vertices
//
//
////        computePaths(A); // run Dijkstra
////        System.out.println("Distance to " + S + ": " + S.minDistance);
////        List<Vertex> path = getShortestPathTo(S);
////        System.out.println("Path: " + path);
//    }



}
