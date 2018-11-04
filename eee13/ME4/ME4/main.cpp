#include <iostream>
#include <vector>

#ifndef LOG_DEBUG
#define LOG_DEBUG 0
#endif

using namespace std;

class Node {
private:
    int weight = 0;
    Node* leftNode = nullptr;
    Node* rightNode = nullptr;
    int bin = 0;
public:
    Node() = default;
    
    Node(int weight) {
        this->weight = weight;
    }
    
    void printContents() {
        cout << weight << " ";
        if(leftNode) leftNode->printContents();
        if(rightNode) rightNode->printContents();
    }
    
    void addNode(Node* node) {
        if(node->getWeight() <= this->weight) {
            if(leftNode) leftNode->addNode(node);
            else leftNode = node;
        } else {
            if(rightNode) rightNode->addNode(node);
            else rightNode = node;
        }
    }
    
    Node* getNode(int weight) {
        if(this->weight == weight && bin == 0) return this;
        else if(!hasChildren()) {
            return nullptr;
        } else {
            Node* n;
            if(leftNode && weight <= this->weight) {
                n = leftNode->getNode(weight);
            } else if(rightNode && weight > this->weight) {
                n = rightNode->getNode(weight);
            } else {
                n = nullptr;
            }
            return n;
        }
    }
    
    bool hasChildren() {
        if(leftNode || rightNode) return true;
        else return false;
    }
    
    int getWeight() {
        return weight;
    }
    
    void setBin(int bin) {
        this->bin = bin;
    }
    
    int getBin() {
        return bin;
    }
    
    void unbin() {
        bin = 0;
    }
    
    void operator=(Node* node) {
        this->weight = node->weight;
        this->leftNode = node->leftNode;
        this->rightNode = node->rightNode;
        this->bin = node->bin;
    }
};

class Bin {
private:
    int binNum = 0;
    int weight = 0;
    int targetWeight = 0;
    vector<Node*> nodes;
public:
    Bin(int binNum, int targetWeight) {
        this->binNum = binNum;
        this->targetWeight = targetWeight;
    }
    
    void addNode(Node* node) {
        weight += node->getWeight();
        node->setBin(binNum);
        nodes.push_back(node);
    }
    
    Node* removeLatestNode() {
        Node* n = nodes.back();
        weight -= n->getWeight();
        nodes.pop_back();
        n->unbin();
        return n;
    }
    
    int getWeight() {
        return weight;
    }
    
    bool isFull() {
        if(weight == targetWeight) return true;
        else return false;
    }
    
    bool willBeOverloaded(int x) {
        if(weight + x > targetWeight) return true;
        else return false;
    }
};

void organize(Node* rootNode, int bins, int weightPerBin) {
#if LOG_DEBUG
    cout << "Weight per bin: " << weightPerBin << endl;
#endif
    for(int i = 1; i <= bins; i++) {
        Bin bin(i, weightPerBin);
        int x = weightPerBin;
        while(!bin.isFull()) {
            while(x > 0) {
                Node* n = rootNode->getNode(x);
                if (n) {
#if LOG_DEBUG
                    cout << "bin " << i << " : ";
                    cout << n->getWeight() << endl;
#endif
                    if (!bin.willBeOverloaded(n->getWeight())) {
#if LOG_DEBUG
                        cout << "adding to bin " << i << " : " << n->getWeight() << endl;
#endif
                        bin.addNode(n);
                        x = weightPerBin - bin.getWeight();
                        if(bin.isFull()) break;
                    } else {
                        x--;
                    }
                } else {
                    x--;
                }
            }
            if(!bin.isFull()) {
                Node* n = bin.removeLatestNode();
#if LOG_DEBUG
                cout << "removing from bin " << i << " : " << n->getWeight() << endl;
#endif
                x = n->getWeight() - 1;
            }
        }
    }
}

int getWeightPerBin(int* weights, int n, int bins) {
    int weight = 0;
    for(int i = 0; i < n; i++) {
        weight += weights[i];
    }
    return weight/bins;
}

int main() {
    int n;
    int *weights;
    int bins;
    
    cin >> n;
    
    weights = new int[n];
    
    for(int i = 0; i < n; i++)
        cin >> weights[i];
    cin >> bins;
    
    Node nodes[n];
    nodes[0] = new Node(weights[0]); //the root node
    for(int i = 1; i < n; i++) {
        nodes[i] = new Node(weights[i]);
        nodes[0].addNode(&nodes[i]);
    }
    
    organize(&nodes[0], bins, getWeightPerBin(weights, n, bins));
    
    for(int i = 0; i < n; i++) {
        cout << nodes[i].getBin() << " ";
    }
    
    return 0;
}
