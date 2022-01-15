// License: Apache-2.0
// Copyright Vladimir Sitnikov, 2021
// See https://github.com/vlsi/github-actions-random-matrix

class Axis {
  constructor({name, title, values}) {
    this.name = name;
    this.title = title;
    this.values = values;
    // If all entries have same weight, the axis has uniform distribution
    this.uniform = values.reduce((a, b) => a === (b.weight || 1) ? a : 0, values[0].weight || 1) !== 0
    this.totalWeight = this.uniform ? values.length : values.reduce((a, b) => a + (b.weight || 1), 0);
  }

  static matches(row, filter) {
    if (typeof filter === 'function') {
      return filter(row);
    }
    if (Array.isArray(filter)) {
      // e.g. row={os: 'windows'}; filter=[{os: 'linux'}, {os: 'linux'}]
      return filter.find(v => Axis.matches(row, v));
    }
    if (typeof filter === 'object') {
      // e.g. row={jdk: {name: 'openjdk', version: 8}}; filter={jdk: {version: 8}}
      for (const [key, value] of Object.entries(filter)) {
        if (!row.hasOwnProperty(key) || !Axis.matches(row[key], value)) {
          return false;
        }
      }
      return true;
    }
    return row === filter;
  }

  pickValue(filter) {
    let values = this.values;
    if (filter) {
      values = values.filter(v => Axis.matches(v, filter));
    }
    if (values.length === 0) {
      const filterStr = typeof filter === 'string' ? filter.toString() : JSON.stringify(filter);
      throw Error(`No values produces for axis '${this.name}' from ${JSON.stringify(this.values)}, filter=${filterStr}`);
    }
    if (values.length === 1) {
      return values[0];
    }
    if (this.uniform) {
      return values[Math.floor(Math.random() * values.length)];
    }
    const totalWeight = !filter ? this.totalWeight : values.reduce((a, b) => a + (b.weight || 1), 0);
    let weight = Math.random() * totalWeight;
    for (let i = 0; i < values.length; i++) {
      const value = values[i];
      weight -= value.weight || 1;
      if (weight <= 0) {
        return value;
      }
    }
    return values[values.length - 1];
  }
}

class MatrixBuilder {
  constructor() {
    this.axes = [];
    this.axisByName = {};
    this.rows = [];
    this.duplicates = {};
    this.excludes = [];
    this.includes = [];
    this.failOnUnsatisfiableFilters = false;
  }

  /**
   * Specifies include filter (all the generated rows would comply with all the include filters)
   * @param filter
   */
  include(filter) {
    this.includes.push(filter);
  }

  /**
   * Specifies exclude filter (e.g. exclude a forbidden combination)
   * @param filter
   */
  exclude(filter) {
    this.excludes.push(filter);
  }

  addAxis({name, title, values}) {
    const axis = new Axis({name, title, values});
    this.axes.push(axis);
    this.axisByName[name] = axis;
    return axis;
  }

  setNamePattern(names) {
    this.namePattern = names;
  }

  /**
   * Returns true if the row matches the include and exclude filters.
   * @param row input row
   * @returns {boolean}
   */
  matches(row) {
    return (this.excludes.length === 0 || !this.excludes.find(f => Axis.matches(row, f))) &&
           (this.includes.length === 0 || this.includes.find(f => Axis.matches(row, f)));
  }

  failOnUnsatisfiableFilters(value) {
    this.failOnUnsatisfiableFilters = value;
  }

  /**
   * Adds a row that matches the given filter to the resulting matrix.
   * filter values could be
   *  - literal values: filter={os: 'windows-latest'}
   *  - arrays: filter={os: ['windows-latest', 'linux-latest']}
   *  - functions: filter={os: x => x!='windows-latest'}
   * @param filter object with keys matching axes names
   * @returns {*}
   */
  generateRow(filter) {
    let res;
    if (filter) {
      // If matching row already exists, no need to generate more
      res = this.rows.find(v => Axis.matches(v, filter));
      if (res) {
        return res;
      }
    }
    for (let i = 0; i < 142; i++) {
      res = this.axes.reduce(
        (prev, next) =>
          Object.assign(prev, {
            [next.name]: next.pickValue(filter ? filter[next.name] : undefined)
          }),
        {}
      );
      if (!this.matches(res)) {
        continue;
      }
      const key = JSON.stringify(res);
      if (!this.duplicates.hasOwnProperty(key)) {
        this.duplicates[key] = true;
        res.name =
          this.namePattern.map(axisName => {
            let value = res[axisName];
            const title = value.title;
            if (typeof title != 'undefined') {
              return title;
            }
            const computeTitle = this.axisByName[axisName].title;
            if (computeTitle) {
                return computeTitle(value);
            }
            if (typeof value === 'object' && value.hasOwnProperty('value')) {
                return value.value;
            }
            return value;
          }).filter(Boolean).join(", ");
        this.rows.push(res);
        return res;
      }
    }
    const filterStr = typeof filter === 'string' ? filter.toString() : JSON.stringify(filter);
    const msg = `Unable to generate row for ${filterStr}. Please check include and exclude filters`;
    if (this.failOnUnsatisfiableFilters) {
      throw Error(msg);
    } else {
      console.warn(msg);
    }
  }

  generateRows(maxRows, filter) {
    for (let i = 0; this.rows.length < maxRows && i < maxRows; i++) {
      this.generateRow(filter);
    }
    return this.rows;
  }

  /**
   * Computes the number of all the possible combinations.
   * @returns {{bad: number, good: number}}
   */
  summary() {
   let position = -1;
   let indices = [];
   let values = {};
   const axes = this.axes;
   function resetValuesUpTo(nextPosition) {
     for(let i=0; i<nextPosition; i++) {
       const axis = axes[i];
       values[axis.name] = axis.values[0];
       indices[i] = 1; // next index
     }
     position = 0;
   }

   function nextAvailablePosition() {
    let size = axes.length;
    for (let i = position; i < size; i++) {
      if (indices[i] < axes[i].values.length) {
        return i;
      }
    }
    return -1;
   }
   // The first initialization of the values
   resetValuesUpTo(this.axes.length);
   let good = 0;
   let bad = 0;
   while (true) {
     if (indices[position] < this.axes[position].values.length) {
       // Advance iterator at the current position if possible
       const axis = this.axes[position];
       values[axis.name] = axis.values[indices[position]];
       indices[position]++;
     } else {
       // Advance the next iterator, and reset [0..nextPosition)
       position++;
       let nextPosition = nextAvailablePosition();
       if (nextPosition === -1) {
         break;
       }
       const axis = this.axes[nextPosition];
       values[axis.name] = axis.values[indices[nextPosition]];
       indices[nextPosition]++;
       resetValuesUpTo(nextPosition);
     }
     if (this.matches(values)) {
       good++;
     } else {
       bad++;
     }
   }
   return {good: good, bad: bad};
  }
}

module.exports = {Axis, MatrixBuilder};
