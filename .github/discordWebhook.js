const axios = require('axios');

const webhookURL = process.env.DISCORD_WEBHOOK;
const commit = process.env.GITHUB_SHA;
const runNumber = process.env.GITHUB_RUN_NUMBER;
const repoName = process.env.GITHUB_REPOSITORY;
const pushUser = process.env.PUSH_USER;
const runID = process.env.RUN_ID;

function sendBuildStatus(status) {
  let title, description, color, buildOutput;
  if (status === 'success') {
    title = 'Build Successful';
    description = 'The build of the project was successful. Details...';
    color = 3066993; // green
    buildOutput = `https://github.com/HeliosMinecraft/HeliosClient/actions/runs/${runID}`;
  } else if (status === 'failure') {
    title = 'Build Failed';
    description = 'The latest build of the project failed. Details...';
    color = 15158332; // red
    buildOutput = `https://github.com/HeliosMinecraft/HeliosClient/actions`;
  }
  const shortCommit = commit.substring(0, 7);
  const data = {
    embeds: {
      title: title,
      description: description,
      color: color,
      url: "https://github.com/HeliosMinecraft/HeliosClient",
     fields: [
        {
          name: 'Actor',
          value: pushUser,
          inline: true
        },
        {
          name: 'Commit',
          value: `[${shortCommit}](https://github.com/${repoName}/commit/${commit})`,
          inline: true
        },
        {
          name: 'Run Number',
          value: runNumber,
          inline: true
        },
        {
          name: 'Build Output',
          value: `[Build](${buildOutput})`,
          inline: true
        }
      ],
      footer: {
        text: 'From Github Actions',
      },
      timestamp: new Date().toISOString()
    }
  };

  axios.post(webhookURL, {
  username: `HeliosClient`,
  avatar_url: `https://media.discordapp.net/attachments/1216707490570309744/1216798445684264990/icon.png?ex=6601b2dc&is=65ef3ddc&hm=6937eb0df5b51988369fd611ed5ee55c48ef2f906fba8caf572f7d0ea0a29c3f&=&format=webp&quality=lossless`,
  embeds: [data.embeds] // send the embeds as an array
})
.then(response => console.log(response.data))
.catch((error) => {
  console.error('Error:', error);
});
}

// Call the function with the build status
const status = process.argv[2]; // get the status from the command line arguments
sendBuildStatus(status); // 'success' or 'failure'