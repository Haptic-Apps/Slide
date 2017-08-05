document.addEventListener('DOMContentLoaded', () => {
  const elChangelog = document.getElementById('changelogSub');
  fetch('https://cdn.rawgit.com/ccrama/Slide/ba7c3052/CHANGELOG.md')
    .then((res) => {
      return res.text();
    })
    .then((text) => {
      console.log(text);
      let tArr = text.trim().split('\n');
      tArr = tArr.slice(0, 3);
      elChangelog.innerHTML = marked(tArr.join(String.fromCharCode(13)));
    });
});
