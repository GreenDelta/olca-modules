% Support product systems in product systems
% Michael Srocka
% December 2018

## Background
A product system in openLCA is currently a set of processes that are connected
by their intermediate product and waste flows. A specific amount of a product
output (or waste input if a waste treatment system is modelled) is the
quantitative reference (the final demand) of such a system. The idea of this
extension is to not only allow processes but also the use of other product
systems as the building blocks of a product system. The result can be a
product system as a hierarchy of other systems that reduces the complexity
on each level and thus would be easier to understand and maintain. It would
also allow to model, use, and share product systems as reusable components for
other systems. Also, features like modelling life cycle stages as separate
product systems would be possible with this extension.

## Process links
A links between two processes in an openLCA product system is stored with an
instance of the `ProcessLink` type. We add an additional field `isSystemLink`
to this type to indicate whether the provider is a product system:

```ml
datatype ProcessLink =
  ProcessLink of {
    flowId:       int64,
    providerId:   int64,
    processId:    int64,
    exchangeId:   int64,
    isSystemLink: bool}
```

We also add the field `is_system_link` to the table `tbl_process_links`:

TODO: also for MySQL:

```sql
CREATE TABLE tbl_process_links (
	f_product_system  BIGINT,
	f_provider        BIGINT,
	f_flow            BIGINT,
	f_process         BIGINT,
	f_exchange        BIGINT,
	is_system_link    SMALLINT default 0
);
```

Existing databases can be updated with the following query:

```sql
-- TODO
```

Currently the processes of a product system are also stored in the field
`processes` of the `ProductSystem` type. We have to think about whether we
replace or remove this field (the content is anyhow redundant as this
information is already contained in the process links).
