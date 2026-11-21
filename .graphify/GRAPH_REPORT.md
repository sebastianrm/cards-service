# Graph Report - .  (2026-11-08)

## Corpus Check
- Corpus is ~793 words - fits in a single context window. You may not need a graph.

## Summary
- 51 nodes · 51 edges · 12 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output
- Edge kinds: inherits: 15 · contains: 13 · method: 12 · imports: 11


## Input Scope
- Requested: auto
- Resolved: all (source: default-auto)
- Included files: 19 · Candidates: recursive
- Excluded: 0 untracked · 0 ignored · 0 sensitive · 0 missing committed
## God Nodes (most connected - your core abstractions)
1. `CardRepository` - 8 edges
2. `ConsumptionRepository` - 6 edges
3. `CustomerProfileViewRepository` - 5 edges
4. `CustomerRiskViewRepository` - 5 edges
5. `ProcessedEventRepository` - 5 edges
6. `CardsServiceApplication` - 2 edges
7. `CardService` - 2 edges
8. `CardController` - 2 edges
9. `Card` - 1 edges
10. `Consumption` - 1 edges

## Surprising Connections (you probably didn't know these)
- `CardRepository` --inherits--> `ReactiveMongoRepository`  [EXTRACTED]
  src/main/java/com/yanki/cards/domain/repository/CardRepository.java →   _Bridges community 0 → community 2_
- `CardRepository` --inherits--> `String`  [EXTRACTED]
  src/main/java/com/yanki/cards/domain/repository/CardRepository.java →   _Bridges community 0 → community 3_
- `ConsumptionRepository` --inherits--> `ReactiveMongoRepository`  [EXTRACTED]
  src/main/java/com/yanki/cards/domain/repository/ConsumptionRepository.java →   _Bridges community 1 → community 2_
- `ConsumptionRepository` --inherits--> `String`  [EXTRACTED]
  src/main/java/com/yanki/cards/domain/repository/ConsumptionRepository.java →   _Bridges community 1 → community 3_
- `CustomerProfileViewRepository` --inherits--> `ReactiveMongoRepository`  [EXTRACTED]
  src/main/java/com/yanki/cards/domain/repository/CustomerProfileViewRepository.java →   _Bridges community 4 → community 2_

## Communities

### Community 0 - "Community 0"
Cohesion: 0.22
Nodes (3): Card, CardRepository, CardController

### Community 1 - "Community 1"
Cohesion: 0.50
Nodes (2): Consumption, ConsumptionRepository

### Community 2 - "Community 2"
Cohesion: 0.60
Nodes (3): CustomerRiskView, ReactiveMongoRepository, CustomerRiskViewRepository

### Community 3 - "Community 3"
Cohesion: 0.50
Nodes (3): ProcessedEvent, ProcessedEventRepository, String

### Community 4 - "Community 4"
Cohesion: 0.67
Nodes (2): CustomerProfileView, CustomerProfileViewRepository

### Community 5 - "Community 5"
Cohesion: 0.67
Nodes (1): CardsServiceApplication

### Community 6 - "Community 6"
Cohesion: 0.67
Nodes (1): CardService

### Community 7 - "Community 7"
Cohesion: 1.00
Nodes (1): Card

### Community 8 - "Community 8"
Cohesion: 1.00
Nodes (1): Consumption

### Community 9 - "Community 9"
Cohesion: 1.00
Nodes (1): CustomerProfileView

### Community 10 - "Community 10"
Cohesion: 1.00
Nodes (1): CustomerRiskView

### Community 11 - "Community 11"
Cohesion: 1.00
Nodes (1): ProcessedEvent

## Knowledge Gaps
- **5 isolated node(s):** `Card`, `Consumption`, `CustomerProfileView`, `CustomerRiskView`, `ProcessedEvent`
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 1`** (2 nodes): `Consumption`, `ConsumptionRepository`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 4`** (2 nodes): `CustomerProfileView`, `CustomerProfileViewRepository`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 5`** (1 nodes): `CardsServiceApplication`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 6`** (1 nodes): `CardService`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 7`** (1 nodes): `Card`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 8`** (1 nodes): `Consumption`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 9`** (1 nodes): `CustomerProfileView`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 10`** (1 nodes): `CustomerRiskView`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 11`** (1 nodes): `ProcessedEvent`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CardRepository` connect `Community 0` to `Community 2`, `Community 3`?**
  _High betweenness centrality (0.123) - this node is a cross-community bridge._
- **Why does `ConsumptionRepository` connect `Community 1` to `Community 2`, `Community 3`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **Why does `CustomerProfileViewRepository` connect `Community 4` to `Community 2`, `Community 3`?**
  _High betweenness centrality (0.036) - this node is a cross-community bridge._
- **What connects `Card`, `Consumption`, `CustomerProfileView` to the rest of the system?**
  _5 weakly-connected nodes found - possible documentation gaps or missing edges._